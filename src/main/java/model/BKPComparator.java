package model;

import java.util.Comparator;

class BKPComparator implements Comparator<Integer> {

    @Override
    public int compare(Integer d1, Integer d2) {
        String s1 = String.valueOf(d1);
        String s2 = String.valueOf(d2);
        int minLength = Math.min(s1.length(), s2.length());

        for (int i = 0; i < minLength; i++){
            if (s1.charAt(i) != s2.charAt(i)){
                return s1.charAt(i) - s2.charAt(i);
            }
        }
        return Integer.parseInt(s1) - Integer.parseInt(s2);
    }
}