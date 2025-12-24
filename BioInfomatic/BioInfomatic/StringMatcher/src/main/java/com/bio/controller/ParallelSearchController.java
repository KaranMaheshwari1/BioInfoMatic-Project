// src/main/java/com/example/bioinfo/controller/ParallelSearchController.java
package com.bio.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bio.model.MatchRequest;
import com.bio.model.MatchResult;
import com.bio.service.ParallelGenericSearchService;

@RestController
@RequestMapping("/api/bioinfo/parallel")
@CrossOrigin(origins = "*") // Allow CORS for all origins
public class ParallelSearchController {

    private final ParallelGenericSearchService parallelService;

    public ParallelSearchController(ParallelGenericSearchService parallelService) {
        this.parallelService = parallelService;
    }

    /**
     * Runs the specified algorithm (e.g., Naive, KMP, etc.) dynamically
     * in a load-balanced parallel fashion on a large DNA sequence.
     *
     * @param request Contains the pattern and number of chunks to divide the genome into.
     * @return Combined match result after parallel execution.
     */
    @PostMapping("/run-dynamic")
    public ResponseEntity<MatchResult> runDynamicParallelSearch(@RequestBody MatchRequest request) {
        // --- Input Validation ---

        // check system Core CPU

        if (request.getPattern() == null || request.getPattern().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MatchResult("Pattern is required."));
        }

        // Validate number of chunks
        if (request.getNumChunks() <= 0) {
            return ResponseEntity
                    .badRequest()
                    .body(new MatchResult("A positive number of chunks (numChunks) is required."));
        }

        try {
            // Get available logical CPU cores for the system (where the API is running)
            int availableCores = Runtime.getRuntime().availableProcessors();
            // Execute and aggregate results from all threads
            MatchResult result = parallelService.executeParallelSearch(request);

            // Log performance metrics
            double timeMs = (double) result.getExecutionTimeNs() / 1_000_000.0;
            System.out.printf(
                    "Dynamic Parallel Search (%s) on 10M bases completed in %.4f ms. Matches: %d. System Cores: %d%n",
                    result.getAlgorithm(),
                    timeMs,
                    result.getMatchIndices().size(),
                    result.getAvailableCores()
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MatchResult("Parallel execution failed: " + e.getMessage()));
        }
    }


}
