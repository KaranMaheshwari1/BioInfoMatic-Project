package com.bio.controller;

import com.bio.model.MatchRequest;
import com.bio.model.MatchResult;
import com.bio.service.SequentialSearchService; // Renamed from NaiveSearchService
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bioinfo/Seqsearch")
@CrossOrigin(origins = "*")
public class SeqSearchControler {

    private final SequentialSearchService searchService;

    public SeqSearchControler(SequentialSearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping("/naive-search")
    public ResponseEntity<MatchResult> runNaive(@RequestBody MatchRequest request) {
        return runSearch(request, "naive");
    }

    @PostMapping("/kmp-search")
    public ResponseEntity<MatchResult> runKMP(@RequestBody MatchRequest request) {
        return runSearch(request, "kmp");
    }

    @PostMapping("/bm-search")
    public ResponseEntity<MatchResult> runBM(@RequestBody MatchRequest request) {
        return runSearch(request, "bm");
    }

    @PostMapping("/suffix-search")
    public ResponseEntity<MatchResult> runSuffix(@RequestBody MatchRequest request) {
        return runSearch(request, "suffix");
    }

    private ResponseEntity<MatchResult> runSearch(MatchRequest request, String algo) {
        try {
            MatchResult result = searchService.executeSearch(request, algo);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new MatchResult("Error: " + e.getMessage()));
        }
    }
}