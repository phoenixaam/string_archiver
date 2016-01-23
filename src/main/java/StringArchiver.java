import java.io.*;
import java.util.*;

public class StringArchiver {
    private static final int MIN_SUBSTRING_LENGTH = 3;

    /**
     * Object for storing info about repeated subStrings
     */

    private static class SubstringObject implements Serializable, Comparable {

        private static int nextSubstringObjectId = 0;

        private final int substringObjectId;
        private int startIndex;
        private int length;

        private final List<Integer> entryIndexes;

        public SubstringObject() {
            this.substringObjectId = nextSubstringObjectId;
            nextSubstringObjectId++;
            entryIndexes = new ArrayList<>();
        }

        public int getStartIndex() {
            return startIndex;
        }

        public void setStartIndex(int startIndex) {
            this.startIndex = startIndex;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public List<Integer> getEntryIndexes() {
            return Collections.unmodifiableList(entryIndexes);
        }

        public int getSubstringObjectId() {
            return substringObjectId;
        }

        public void clearEntryIndexes() {
            entryIndexes.clear();
        }

        public boolean addEntryIndex(int index) {
            return entryIndexes.add(index);
        }

        @Override
        public int compareTo(Object o) {
            if (this == o) return 0;
            if (o == null || !(o instanceof SubstringObject)) {
                return -1;
            }
            return this.getSubstringObjectId() - ((SubstringObject) o).getSubstringObjectId();
        }
    }

    /**
     * Form list of repeated subStrings
     * and replace entrys in origin string accordingly to this list
     */
    public byte[] compress(final String origin) {
        Objects.requireNonNull(origin, "string for compressing can't be empty");

        List<SubstringObject> subStrings = new LinkedList<>();
        String changedString = origin;

        boolean done = false;
        do {
            SubstringObject substringObject = findMaxSubstring(changedString);
            if (substringObject.getLength() >= MIN_SUBSTRING_LENGTH) {
                subStrings.add(substringObject);
                changedString = removeSubstring(changedString, substringObject);
            } else {
                done = true;
            }
        } while (!done);
        PrintArchiveInfo(subStrings, origin, changedString);

        return getBytes(subStrings, changedString);
    }

    /**
     * Write list and changed string to the byte array
     *
     * @param subStrings    list of repeated subStrings
     * @param changedString string with replaced subStrings accordingly to list
     * @return byte[]
     */
    private byte[] getBytes(final List<SubstringObject> subStrings, final String changedString) {
        byte[] compressedBytes = new byte[0];
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(subStrings);
            out.writeObject(changedString);
            compressedBytes = bytes.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return compressedBytes;
    }

    private SubstringObject findMaxSubstring(final String origin) {
        Objects.requireNonNull(origin);
        final SubstringObject substringObject = new SubstringObject();
        substringObject.setStartIndex(0);
        substringObject.setLength(0);
        char[] orig = origin.toCharArray();
        int tmpMax;
        for (int i = 0; i < orig.length; i++) {
            String tmpAdd = "";
            for (int j = i + 1; j < orig.length; j++) {
                if (orig[i] == orig[j] && checkSymbol(orig[i])) {
                    tmpMax = 1;
                    int k = i + tmpMax;
                    int l = j + tmpMax;
                    while (k < orig.length && l < orig.length && orig[k] == orig[l] && checkSymbol(orig[k])) {
                        tmpMax++;
                        k++;
                        l++;
                    }
                    if (tmpMax >= MIN_SUBSTRING_LENGTH && tmpMax > substringObject.getLength()) {
                        substringObject.setLength(tmpMax);
                        substringObject.setStartIndex(i);
                        substringObject.clearEntryIndexes();
                        substringObject.addEntryIndex(j);
                        tmpAdd = origin.substring(i, i + tmpMax - 1);
                    } else if (tmpMax >= MIN_SUBSTRING_LENGTH && tmpMax == substringObject.getLength() && tmpAdd.equals(origin.substring(i, i + tmpMax - 1))) {
                        substringObject.addEntryIndex(j);
                    }
                }
            }

        }
        return substringObject;
    }

    private String removeSubstring(final String changedString, final SubstringObject substringObject) {
        Objects.requireNonNull(changedString);
        Objects.requireNonNull(substringObject);
        StringBuilder sb = new StringBuilder(changedString);
        List<Integer> entryIndexes = new LinkedList<>(substringObject.getEntryIndexes());
        Collections.sort(entryIndexes, Comparator.reverseOrder());
        final Queue<Integer> entryIndexQueue = new LinkedList<>(entryIndexes);
        while (!entryIndexQueue.isEmpty()) {
            int indexFrom = entryIndexQueue.poll();
            int length = substringObject.getLength();
            sb.replace(indexFrom, indexFrom + length, "&" + substringObject.getSubstringObjectId());
        }
        return sb.toString();
    }

    /**
     * Read from bite array 1) List of subStrings 2) compressed string
     * replace in compressed string all special marks accordingly to
     * list of subStrings, starting from last index in List
     *
     * @param compressedBytes byte[]
     * @return decompressed String
     */
    public String decompress(final byte[] compressedBytes) {
        List<SubstringObject> subStrings = new LinkedList<>();
        String compressedString = "";
        try (ByteArrayInputStream bytes = new ByteArrayInputStream(compressedBytes);
             ObjectInputStream in = new ObjectInputStream(bytes)) {
            subStrings = (List<SubstringObject>) in.readObject();
            compressedString = (String) in.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return addSubstrings(compressedString, subStrings);
    }

    private String addSubstrings(final String compressedString, final List<SubstringObject> subStrings) {
        Objects.requireNonNull(compressedString);
        Objects.requireNonNull(subStrings);
        StringBuilder result = new StringBuilder(compressedString);
        final Deque<SubstringObject> substringObjectsQueue = new LinkedList<>(subStrings);
        while (!substringObjectsQueue.isEmpty()) {
            final SubstringObject substringObject = substringObjectsQueue.pollLast();
            final int startIndex = substringObject.getStartIndex();
            final int length = substringObject.getLength();
            final int substringObjectId = substringObject.getSubstringObjectId();
            final String substring = result.substring(startIndex, startIndex + length);
            final List<Integer> entryIndexes = new LinkedList<>(substringObject.getEntryIndexes());
            Collections.sort(entryIndexes, Comparator.naturalOrder());
            final Queue<Integer> entryIndexQueue = new LinkedList<>(entryIndexes);
            while (!entryIndexQueue.isEmpty()) {
                int indexFrom = entryIndexQueue.poll();
                int indexTo = indexFrom + Integer.valueOf(substringObjectId).toString().length() + 1;
                if (result.substring(indexFrom, indexTo).equals("&" + substringObjectId)) {
                    result.replace(indexFrom, indexTo, substring);
                } else {
                    throw new RuntimeException("Incorrect substringObjectId!");
                }
            }
        }
        return result.toString();
    }

    private void PrintArchiveInfo(final List<SubstringObject> subStrings, final String originalString, final String changedString) {
        System.out.println(changedString);
        System.out.println("ArchivedLength=" + changedString.length());
        System.out.println("OriginalLength=" + originalString.length());
        System.out.println("Compressed Elements:");
        for (SubstringObject substringObject : subStrings) {
            System.out.println("ID=" + substringObject.getSubstringObjectId() + "\t" +
                    "StartIndex=" + substringObject.getStartIndex() + "\t" +
                    "Length=" + substringObject.getLength() + "\t" +
                    "EntryIndexes:" + substringObject.getEntryIndexes());
        }
    }

    private boolean checkSymbol(char c) {
        if (c == '&' || Character.isDigit(c)) {
            return false;
        } else {
            return true;
        }
    }

    public static void main(String[] args) {
        final StringArchiver stringArchiver = new StringArchiver();
        String origin = "Вам нужно разработать класс который будет эмулировать архивацию\n" +
                "В нем будет два метода Первый (compress) принимает строку и возвращает байтовый массив\n" +
                "второй (decompress) наоборот\n" +
                "compress будет сначала сканировать строку и искать максимальный размер повторяющейся\n" +
                "подстроки\n" +
                "Например для 'abc' это будет 1 для 'ababc' 2\n" +
                "Потом во время второго сканирования исходная строка превращается в байтовый массив путем\n" +
                "замены повторяющихся строк на некоторые уникальные идентификаторы\n" +
                "decompress принимает байтовый массив и заменяет идентификаторы обратно на подстроки с\n" +
                "целью получения исходной строки";

        byte[] compressedBytes = stringArchiver.compress(origin);
        String decompressedString = stringArchiver.decompress(compressedBytes);
        System.out.println(decompressedString);
    }


}
