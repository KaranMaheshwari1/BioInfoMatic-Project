package com.bio.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.stream.Collectors;

public class GenomeDataGeneratorPro {

    // --- Configuration ---
    private static final long FIXED_SEED = 42L;
    private static final String FILE_NAME = "genome_data.txt"; // Standardized filename

    // --- MEMORY CACHE (RAM) ---
    // This variable stores the genome in memory. Static means it stays alive as long as the app runs.
    private static String cachedGenomeData = null;
    // --------------------------

    /**
     * Generates a sequence, saves it to Disk, AND updates the Memory Cache.
     */
    public static Path generateAndSaveFile(String pattern, int size) throws IOException {
        Path filePath = Path.of(FILE_NAME);

        // --- STEP 1: CLEANUP ---
        if (Files.exists(filePath)) {
            Files.delete(filePath);
            System.out.println("Old file deleted from disk: " + filePath.toAbsolutePath());
        }

        // Clear the old cache to ensure we don't serve stale data
        cachedGenomeData = null;

        // --- STEP 2: PREPARE DYNAMIC ALPHABET ---
        String p = pattern.toUpperCase();
        String uniqueChars = p.chars()
                .distinct()
                .mapToObj(c -> String.valueOf((char) c))
                .collect(Collectors.joining());

        char[] dynamicBases = uniqueChars.toCharArray();

        if (dynamicBases.length == 0) {
            throw new IOException("Pattern must contain at least one character to generate a file.");
        }

        System.out.println("Generating " + size + " char sequence using alphabet " + uniqueChars + " from pattern: " + p);

        Random random = new Random(FIXED_SEED);

        // 3. Generate the sequence
        // Pre-allocating size improves performance significantly during generation
        StringBuilder genome = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            genome.append(dynamicBases[random.nextInt(dynamicBases.length)]);
        }

        // 4. Inject known matches
        int pLen = p.length();
        int[] injectionPoints = {
                1000,
                size / 2,
                size - pLen - 1000
        };

        for (int idx : injectionPoints) {
            if (idx >= 0 && idx + pLen <= size) {
                genome.replace(idx, idx + pLen, p);
            }
        }

        // 5. Write to file (Persistence)
        String finalData = genome.toString();
        Files.writeString(filePath, finalData);
        System.out.println("File successfully generated and saved to Disk.");

        // --- STEP 6: UPDATE CACHE ---
        // Store the String in RAM so subsequent searches don't read from disk
        cachedGenomeData = finalData;
        System.out.println("Genome data cached in RAM (Size: " + finalData.length() + ")");

        return filePath;
    }

    /**
     * Helper to read the file.
     * INTELLIGENT CACHING: Returns data from RAM if available; otherwise reads from Disk.
     */
    public static String readGenomeFile() throws IOException {
        // 1. Check RAM Cache (Fastest - Nanoseconds)
        if (cachedGenomeData != null) {
            // Uncomment the line below if you want to verify it's working in logs
            // System.out.println("Cache Hit: Serving genome data from RAM.");
            return cachedGenomeData;
        }

        // 2. Fallback to Disk (Slow - Milliseconds)
        Path filePath = Path.of(FILE_NAME);
        if (!Files.exists(filePath)) {
            throw new IOException("Data file not found. Generate it first.");
        }

        System.out.println("Cache Miss: Reading genome data from Disk...");
        cachedGenomeData = Files.readString(filePath); // Load into cache for next time
        return cachedGenomeData;
    }
}