package model;

import java.util.Comparator;

/**
 * Orders BKP codes (Swiss construction cost plan) hierarchically.
 * <p>
 * Codes are compared digit by digit, so each code is followed by its sub-codes
 * before the next code on the same level. Example order:
 * {@code 1, 11, 111, 112, 12, 2, 21, 211}.
 * <p>
 * Expects non-negative, non-null values.
 */
public class BKPComparator implements Comparator<Integer> {

    /**
     * Compares two BKP codes by their digit sequence.
     *
     * @param d1 the first BKP code
     * @param d2 the second BKP code
     * @return a negative value if {@code d1} comes first, a positive value if
     *         {@code d2} comes first, or zero if both are equal
     */
    @Override
    public int compare(Integer d1, Integer d2) {
        String s1 = String.valueOf(d1);
        String s2 = String.valueOf(d2);
        int minLength = Math.min(s1.length(), s2.length());

        for (int i = 0; i < minLength; i++) {
            if (s1.charAt(i) != s2.charAt(i)) {
                return s1.charAt(i) - s2.charAt(i);
            }
        }
        return Integer.parseInt(s1) - Integer.parseInt(s2);
    }
}