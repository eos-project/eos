package eos.util;

public class StringStartPatternFinder
{
    /**
     * Utility class to find repeating pattern
     *
     * @param source Array of strings
     * @return Found pattern
     */
    public String findPattern(String[] source)
    {
        // Edge cases
        if (source == null || source.length == 0) {
            return "";
        }
        if (source.length == 1) {
            return source[0];
        }

        String mask = null;
        for (String row : source) {
            if (mask == null) {
                mask = row;
            } else if (row == null) {
                continue;
            } else if (row.length() == 0) {
                return row;
            } else {
                for (int i = 0; i < mask.length(); i++) {
                    if (row.length() <= i || row.charAt(i) != mask.charAt(i)) {
                        // Taking substring
                        if (i == 0) {
                            // Edge case - empty pattern
                            return "";
                        } else {
                            // Trimming
                            mask = mask.substring(0, i);
                        }
                        break;
                    }
                }
            }
        }

        return mask;
    }
}
