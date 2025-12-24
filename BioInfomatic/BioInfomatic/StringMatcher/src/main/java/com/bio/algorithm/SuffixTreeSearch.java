package com.bio.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.bio.model.MatchResult;

public class SuffixTreeSearch {
    // Implementing Suffix Array + Binary Search for stability on large strings
    public MatchResult search(String text, String pattern) {
        long startTime = System.nanoTime();
        int n = text.length();
        int m = pattern.length();
        List<Integer> matches = new ArrayList<>();

        if (m == 0 || m > n) return new MatchResult("Suffix Array", 0, matches, n, m);

        // 1. Build Suffix Array (O(N log N) or O(N log^2 N))
        // Using Java's sort is O(N log N * avg_compare), can be slow for massive strings
        // but robust for this demo.
        Integer[] suffixArr = new Integer[n];
        for (int i = 0; i < n; i++) suffixArr[i] = i;

        Arrays.sort(suffixArr, (a, b) -> {
            // Optimization: Only compare up to pattern length + 1 for search purposes
            // This makes sorting faster for the specific purpose of finding this pattern
            int len = Math.min(n - a, n - b);
            int lim = Math.min(len, m + 50);
            for (int k = 0; k < lim; k++) {
                char c1 = text.charAt(a + k);
                char c2 = text.charAt(b + k);
                if (c1 != c2) return c1 - c2;
            }
            return (n - a) - (n - b);
        });

        // 2. Binary Search for the Pattern
        int l = 0, r = n - 1;
        while (l <= r) {
            int mid = l + (r - l) / 2;
            int idx = suffixArr[mid];
            String suffix = text.substring(idx, Math.min(idx + m, n));

            int cmp = suffix.compareTo(pattern);
            if (cmp == 0) {
                // Found one, now expand to find all duplicates
                // (Linear scan neighbors - simplified for demo)
                int temp = mid;
                while (temp >= 0 && text.substring(suffixArr[temp], Math.min(suffixArr[temp] + m, n)).equals(pattern)) {
                    matches.add(suffixArr[temp]);
                    temp--;
                }
                temp = mid + 1;
                while (temp < n && text.substring(suffixArr[temp], Math.min(suffixArr[temp] + m, n)).equals(pattern)) {
                    matches.add(suffixArr[temp]);
                    temp++;
                }
                break;
            } else if (cmp < 0) {
                l = mid + 1;
            } else {
                r = mid - 1;
            }
        }

        // Dedup and sort
        matches.sort(Integer::compareTo);
        List<Integer> distinct = new ArrayList<>();
        if (!matches.isEmpty()) {
            distinct.add(matches.get(0));
            for(int i=1; i<matches.size(); i++) {
                if(!matches.get(i).equals(matches.get(i-1))) distinct.add(matches.get(i));
            }
        }

        long executionTimeNs = System.nanoTime() - startTime;
        return new MatchResult("Suffix Array Search", executionTimeNs, distinct, n, m);
    }
}