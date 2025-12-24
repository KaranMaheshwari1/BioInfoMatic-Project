package com.bio.service;

import com.bio.algorithm.BoyerMooreSearch;
import com.bio.algorithm.KMPSearch;
import com.bio.algorithm.NaiveSearch;
import com.bio.algorithm.SuffixTreeSearch;
import com.bio.model.MatchRequest;
import com.bio.model.MatchResult;
import com.bio.utils.GenomeDataGenerator;
import com.bio.utils.GenomeDataGeneratorPro;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class SequentialSearchService {

    public MatchResult executeSearch(MatchRequest request, String algoType) throws IOException {
        String text = GenomeDataGeneratorPro.readGenomeFile();
        String pattern = request.getPattern();

        switch (algoType.toLowerCase()) {
            case "kmp":
                return new KMPSearch().search(text, pattern);
            case "bm":
            case "boyermoore":
                return new BoyerMooreSearch().search(text, pattern);
            case "suffix":
            case "suffixtree":
                return new SuffixTreeSearch().search(text, pattern);
            case "naive":
            default:
                return new NaiveSearch().search(text, pattern);
        }
    }
}