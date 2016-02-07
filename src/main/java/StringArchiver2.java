import java.util.Objects;

public class StringArchiver2 {
    private static final int MIN_SUBSTRING_LENGTH = 3;
    private static final int INIT_SUBSTRINGS_ARRAY_LENGTH = 10;
    private static int nextIndex;
    /**
     * Object for storing info about repeated subStrings
     */
    private String[] subStrings;

    public StringArchiver2() {
        subStrings = new String[INIT_SUBSTRINGS_ARRAY_LENGTH];
        nextIndex = 0;
    }

    private int add(String entry) {
        if (nextIndex + 1 >= subStrings.length) {
            extendArray();
        }
        subStrings[nextIndex] = entry;
        return nextIndex++;
    }

    /**
     * Form array of repeated subStrings
     * and replace entrys in origin string accordingly to this list
     */
    public byte[] compress(final String origin) {
        Objects.requireNonNull(origin, "string for compressing can't be empty");
        String changedString = origin;
        int tmpMax;
        int maxRepeatedLength = 0;
        for (int i = 0; i < changedString.length(); i++) {
            for (int j = i + MIN_SUBSTRING_LENGTH; j < changedString.length(); j++) {
                if (changedString.charAt(i) == changedString.charAt(j) && checkSymbol(changedString.charAt(i))) {
                    tmpMax = 1;
                    int k = i + tmpMax;
                    int l = j + tmpMax;
                    while (k < changedString.length() && l < changedString.length() && k < j && changedString.charAt(k) ==
                            changedString.charAt(l) && checkSymbol(changedString.charAt(k))) {
                        tmpMax++;
                        k++;
                        l++;
                    }
                    if (tmpMax > maxRepeatedLength) {
                        maxRepeatedLength = tmpMax;
                    }
                    if (tmpMax >= MIN_SUBSTRING_LENGTH) {
                        String entry = changedString.substring(i, i + tmpMax);
                        int index = add(entry);
                        changedString = changedString.replaceAll(entry, "&" + index);
                        break;
                    }
                }
            }
        }
        System.out.println("maxRepeatedLength: " + maxRepeatedLength);
        printArchiveInfo2(subStrings, origin, changedString);
        return changedString.getBytes();
    }

    /**
     * Read from bite array
     *
     * @param compressedBytes byte[]
     * @return decompressed String
     */
    public String decompress(final byte[] compressedBytes) {
        String deCompressedString = new String(compressedBytes);

        for (int i = subStrings.length - 1; i >= 0; i--) {
            if (subStrings[i] != null) {
                String id = "&" + i;
                deCompressedString = deCompressedString.replaceAll(id, subStrings[i]);
            }
        }

        return deCompressedString;
    }

    private boolean checkSymbol(char c) {
        if (Character.isLetter(c)) {
            return true;
        } else {
            return false;
        }
    }

    private void extendArray() {
        String[] newSubStrings = new String[subStrings.length * 2];
        System.arraycopy(subStrings, 0, newSubStrings, 0, subStrings.length);
        subStrings = newSubStrings;
    }

    private static void printArchiveInfo2(final String[] subStrings, final String originalString, final String changedString) {
        System.out.println(changedString);
        System.out.println("ArchivedLength=" + changedString.length());
        System.out.println("OriginalLength=" + originalString.length());
        System.out.println("Compressed Elements:");
        for (int i = 0; i < subStrings.length; i++) {
            if (subStrings[i] != null) {
                System.out.println("ID=" + i + "\t" +
                        "Entry: " + subStrings[i] + "\t");
            }
        }
    }

    public static void main(String[] args) {
        final StringArchiver2 stringArchiver = new StringArchiver2();
//        String origin = "aaaaa";
//        String origin = "ababc";
        String origin = "Разработать архиватор строк\n" +
                "Вам нужно разработать класс который будет эмулировать архивацию\n" +
                "В нем будет два метода Первый (compress) принимает строку и возвращает байтовый массив\n" +
                "второй (decompress) наоборот.\n" +
                "Compress будет сначала сканировать строку и искать максимальный размер повторяющейся\n" +
                "подстроки.\n" +
                "Например для \"abc\" это будет 1, для \"ababc\" 2/\n" +
                "Потом во время второго сканирования исходная строка превращается в байтовый массив путем\n" +
                "замены повторяющихся строк на некоторые уникальные идентификаторы.\n" +
                "decompress принимает байтовый массив и заменяет идентификаторы обратно на подстроки с\n" +
                "целью получения исходной строки.";

        byte[] compressedBytes = stringArchiver.compress(origin);
        String decompressedString = stringArchiver.decompress(compressedBytes);
        System.out.println("decompressedString: " + decompressedString);
        if (decompressedString.equals(origin)) {
            System.out.println("decompress successful");
        } else {
            System.out.println("decompress not successful");
        }
    }

}
