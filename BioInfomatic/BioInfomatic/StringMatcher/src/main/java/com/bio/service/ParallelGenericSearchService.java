package com.bio.service;

import com.bio.utils.GenomeDataGeneratorPro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bio.model.MatchRequest;
import com.bio.model.MatchResult;
import com.bio.utils.GenomeDataGenerator;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HardwareAbstractionLayer;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.io.IOException;

@Service
public class ParallelGenericSearchService {

    private final NaiveSearchService naiveSearchService;

    @Value("${app.parallel.num-threads:4}")
    private int NUM_THREADS;

    @Autowired
    public ParallelGenericSearchService(NaiveSearchService naiveSearchService) {
        this.naiveSearchService = naiveSearchService;
    }

    public MatchResult executeParallelSearch(MatchRequest request) throws Exception {

        // 1. Read the Massive Genomic Data from the file
        String pattern = request.getPattern();

        // Read from file
        String text;
        try {
            text = GenomeDataGeneratorPro.readGenomeFile();
        } catch (IOException e) {
            throw new Exception("Failed to read genome file. Ensure the file generation endpoint was successfully called.", e);
        }

        int textLength = text.length();
        int patternLength = pattern.length();

        // Use the number of chunks specified in the request
        final int requestedChunks = request.getNumChunks();

        // Ensure requestedChunks is a positive number, default to NUM_THREADS if invalid
        final int NUM_TASKS = (requestedChunks > 0) ? requestedChunks : NUM_THREADS;

        // 2. Prepare Chunks with Overlap (Master's role: Data Decomposition)
        int chunkSizeBase = textLength / NUM_TASKS;
        int overlapSize = patternLength - 1;

        List<Callable<Set<Integer>>> tasks = new ArrayList<>();

        for (int i = 0; i < NUM_TASKS; i++) {
            final int start = i * chunkSizeBase;
            int end = (i + 1) * chunkSizeBase;

            // Add overlap to all but the last chunk
            if (i < NUM_TASKS - 1) {
                end += overlapSize;
            }

            final String chunk = text.substring(start, Math.min(end, textLength));

            // Create the Callable task (Worker's role)
            tasks.add(() -> {
                List<Integer> chunkMatches;
                String algo = request.getAlgorithm() != null ? request.getAlgorithm().toLowerCase() : "naive";

                // Select Algorithm for this Chunk
                if (algo.contains("kmp")) {
                    chunkMatches = new com.bio.algorithm.KMPSearch().search(chunk, pattern).getMatchIndices();
                } else if (algo.contains("boyer") || algo.contains("bm")) {
                    chunkMatches = new com.bio.algorithm.BoyerMooreSearch().search(chunk, pattern).getMatchIndices();
                } else if (algo.contains("suffix")) {
                    chunkMatches = new com.bio.algorithm.SuffixTreeSearch().search(chunk, pattern).getMatchIndices();
                } else {
                    // Default Naive
                    chunkMatches = new com.bio.algorithm.NaiveSearch().search(chunk, pattern).getMatchIndices();
                }

                // Aggregate and convert local indices to absolute indices
                return chunkMatches.stream()
                        .map(localIndex -> start + localIndex)
                        .collect(Collectors.toSet());
            });
        }

        // 3. Parallel Execution and Aggregation (Master's role: Dispatch & Gather)
        long startTime = System.nanoTime();

        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        int cores = Runtime.getRuntime().availableProcessors();

        // System Info for Metadata
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        long totalMemoryMB = Runtime.getRuntime().totalMemory() / (1024 * 1024);

        // OSHI Hardware Info (Logging only)
        try {
            SystemInfo si = new SystemInfo();
            HardwareAbstractionLayer hal = si.getHardware();
            CentralProcessor cpu = hal.getProcessor();
            System.out.println("CPU: " + cpu.getProcessorIdentifier().getName());
            GlobalMemory memory = hal.getMemory();
            System.out.println("Total RAM: " + memory.getTotal() / (1024*1024*1024) + " GB");
            for (GraphicsCard gc : hal.getGraphicsCards()) {
                System.out.println("GPU: " + gc.getName());
            }
        } catch (Exception e) {
            System.out.println("OSHI info retrieval failed (optional dependency): " + e.getMessage());
        }

        List<Future<Set<Integer>>> futures = executor.invokeAll(tasks);

        Set<Integer> allMatches = new HashSet<>();
        for (Future<Set<Integer>> future : futures) {
            allMatches.addAll(future.get()); // Aggregation and Collection
        }

        executor.shutdown(); // Gracefully shut down the pool
        long endTime = System.nanoTime();

        // 4. Construct Final Result
        MatchResult result = new MatchResult();
        result.setAlgorithm(request.getAlgorithm() + " Parallel (Chunks: " + NUM_TASKS + " | Threads: " + NUM_THREADS + ")");
        result.setExecutionTimeNs(endTime - startTime);
        result.setMatchIndices(new ArrayList<>(allMatches));
        result.setTextLength(textLength);
        result.setPatternLength(patternLength);

        // Set System Properties
        result.setAvailableCores(cores);
        result.setCpuArchitecture(osArch);
        result.setOperatingSystem(osName);
        result.setTotalMemoryMB(totalMemoryMB);

        return result;
    }
}