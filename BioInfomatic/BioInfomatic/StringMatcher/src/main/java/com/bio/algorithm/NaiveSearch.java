package com.bio.algorithm;

import java.util.ArrayList;
import java.util.List;

import com.bio.model.MatchResult;

public class NaiveSearch {

    // Basic Naive String Search (no comparison counter)
    public MatchResult search(String text, String pattern) {
        List<Integer> matchIndices = new ArrayList<>();
        int N = text.length();
        int M = pattern.length();
        long startTime = System.nanoTime();

        // Outer loop over the text
        for (int i = 0; i <= N - M; i++) {
            int j;

            // Inner loop over the pattern
            for (j = 0; j < M; j++) {
                if (text.charAt(i + j) != pattern.charAt(j)) {
                    break; // Mismatch found
                }
            }

            // If pattern[0...M-1] matched
            if (j == M) {
                matchIndices.add(i); // Pattern found at index i
            }
        }

        long endTime = System.nanoTime();
        long executionTimeNs = endTime - startTime;

        return new MatchResult("Naive Search", executionTimeNs, matchIndices, N, M);
    }

    // Improved Naive Search (tracks comparisons for analysis)
    public MatchResult ImproviseSearch(String text, String pattern) {
        List<Integer> matchIndices = new ArrayList<>();
        int N = text.length();
        int M = pattern.length();

        // Counter for character comparisons
        long comparisonCount = 0;

        long startTime = System.nanoTime();

        for (int i = 0; i <= N - M; i++) {
            int j;
            for (j = 0; j < M; j++) {
                // Increment counter on every comparison
                comparisonCount++;

                if (text.charAt(i + j) != pattern.charAt(j)) {
                    break;
                }
            }

            if (j == M) {
                matchIndices.add(i);
            }
        }

        long endTime = System.nanoTime();
        long executionTimeNs = endTime - startTime;

        // Requires MatchResult constructor with comparisonCount parameter
        return new MatchResult("Improvised Naive Search", executionTimeNs, matchIndices, N, M, comparisonCount);
    }
}
