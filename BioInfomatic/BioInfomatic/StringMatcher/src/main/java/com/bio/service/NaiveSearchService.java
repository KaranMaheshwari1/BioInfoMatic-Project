// src/main/java/com/bio/service/NaiveSearchService.java

package com.bio.service;

import com.bio.model.MatchRequest;
import com.bio.model.MatchResult;
import com.bio.utils.GenomeDataGenerator; 
import com.bio.algorithm.NaiveSearch; // Assuming NaiveSearch has the .search(text, pattern) method
import com.bio.utils.GenomeDataGeneratorPro;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;

@Service
public class NaiveSearchService {

    public MatchResult executeSearch(MatchRequest request) throws IOException {
        
        // 1. Read the Massive Genomic Data from the file
        String text = GenomeDataGeneratorPro.readGenomeFile();
        String pattern = request.getPattern();

        // 2. Execute the search and return the result
        // Assuming your NaiveSearch class is an algorithm implementation:
        MatchResult result = new NaiveSearch().search(text, pattern);
        
        result.setAlgorithm("Naive Search");
        
        return result;
    }
    
    // The existing search method that ParallelGenericSearchService relies on:
    public List<Integer> search(String text, String pattern) {
        // Assume the implementation of the core Naive Search logic is here, 
        // which is used by the parallel worker tasks (threads).
        List<Integer> matches = new java.util.ArrayList<>();
        int N = text.length();
        int M = pattern.length();

        if (M == 0 || N < M) {
            return matches;
        }

        for (int i = 0; i <= N - M; i++) {
            int j;
            for (j = 0; j < M; j++) {
                if (text.charAt(i + j) != pattern.charAt(j)) {
                    break;
                }
            }
            if (j == M) {
                matches.add(i);
            }
        }
        return matches;
    }
}