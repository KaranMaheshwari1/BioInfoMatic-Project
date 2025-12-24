package com.bio.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.bio.model.MatchResult;

public class BoyerMooreSearch {
    public MatchResult search(String text, String pattern) {
        List<Integer> matches = new ArrayList<>();
        int n = text.length();
        int m = pattern.length();
        long startTime = System.nanoTime();

        if (m == 0) return new MatchResult("Boyer-Moore", 0, matches, n, m);

        int[] badChar = new int[256];
        Arrays.fill(badChar, -1);
        for (int i = 0; i < m; i++) {
            badChar[pattern.charAt(i)] = i;
        }

        int s = 0;
        while (s <= (n - m)) {
            int j = m - 1;
            while (j >= 0 && pattern.charAt(j) == text.charAt(s + j))
                j--;

            if (j < 0) {
                matches.add(s);
                s += (s + m < n) ? m - badChar[text.charAt(s + m)] : 1;
            } else {
                s += Math.max(1, j - badChar[text.charAt(s + j)]);
            }
        }

        long executionTimeNs = System.nanoTime() - startTime;
        return new MatchResult("Boyer-Moore Search", executionTimeNs, matches, n, m);
    }
}