import java.util.*;

public class TextCompressor {
    public static List<Integer> compress(String uncompressedText) {

        Objects.requireNonNull(uncompressedText, "uncompressed text empty");

        Map<String, Integer> dictionary = new HashMap<String, Integer>();

        int dictionarySize = getStartDictionarySize();

        for( int i = 0; i < dictionarySize; i++) {
            dictionary.put("" + (char) i, i);
        }

        String workWord = "";

        List<Integer> resultCompress = new ArrayList<Integer>();

        for(char currentChar : uncompressedText.toCharArray()) {
            String currentWorkWord = workWord + currentChar;

            if (dictionary.containsKey(currentWorkWord)) {
                workWord = currentWorkWord;
            } else {
                resultCompress.add(dictionary.get(workWord));
                dictionary.put(currentWorkWord, dictionarySize++);
                workWord = "" + currentChar;
            }
        }

        if (!workWord.equals("")) {
            resultCompress.add(dictionary.get(workWord));
        }
        System.out.println(uncompressedText.length());
        System.out.println(resultCompress.size());
        System.out.println(dictionary);
        return resultCompress;
    }

    public static int getStartDictionarySize() {
        int dictionarySize = 1103; // dictionary char with Cyrillic

        return dictionarySize;
    }

    public static String decompress(List<Integer> compressedText) {

        Objects.requireNonNull(compressedText, "compressed text empty");

        Map<Integer, String> dictionary = new HashMap<Integer, String>();

        int dictionarySize = getStartDictionarySize();

        for (int i = 0; i < dictionarySize; i++) {
            dictionary.put(i, "" + (char) i);
        }

        String workWord = "" + (char)(int)compressedText.remove(0);

        StringBuffer resultDecompress = new StringBuffer(workWord);

        for(int code : compressedText) {
            String item;

            if(dictionary.containsKey(code)) {
                item = dictionary.get(code);
            } else if (code == dictionarySize){
                item = workWord + workWord.charAt(0);
            } else {
                throw new IllegalArgumentException("Bad compressed code: " + code);
            }

            resultDecompress.append(item);

            dictionary.put(dictionarySize++, workWord + item.charAt(0));

            workWord = item;

        }

        return resultDecompress.toString();
    }

    public static void main(String[] args) {
        String text = "Вам нужно разработать класс, который будет эмулировать архивацию. В нем будет два метода.";

        List<Integer> compressedText = compress(text);

        System.out.println(compressedText);

        String decompressedText = decompress(compressedText);

        System.out.println(decompressedText);
    }
}
