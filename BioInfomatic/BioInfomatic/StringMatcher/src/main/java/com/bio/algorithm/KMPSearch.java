package com.bio.algorithm;

import java.util.ArrayList;
import java.util.List;
import com.bio.model.MatchResult;

public class KMPSearch {
    public MatchResult search(String text, String pattern) {
        List<Integer> matches = new ArrayList<>();
        int n = text.length();
        int m = pattern.length();
        long startTime = System.nanoTime();

        if (m == 0) return new MatchResult("KMP", 0, matches, n, m);

        // Compute LPS array
        int[] lps = new int[m];
        int len = 0;
        int i = 1;
        lps[0] = 0;
        while (i < m) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else {
                if (len != 0) {
                    len = lps[len - 1];
                } else {
                    lps[i] = 0;
                    i++;
                }
            }
        }

        // Search
        int j = 0; // index for pattern
        i = 0; // index for text
        while (i < n) {
            if (pattern.charAt(j) == text.charAt(i)) {
                j++;
                i++;
            }
            if (j == m) {
                matches.add(i - j);
                j = lps[j - 1];
            } else if (i < n && pattern.charAt(j) != text.charAt(i)) {
                if (j != 0)
                    j = lps[j - 1];
                else
                    i++;
            }
        }

        long executionTimeNs = System.nanoTime() - startTime;
        return new MatchResult("KMP Search", executionTimeNs, matches, n, m);
    }
}