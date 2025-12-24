package com.bio.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.stream.Collectors;

public class GenomeDataGenerator {

    // --- Configuration ---
    private static final long FIXED_SEED = 42L;
    private static final String FILE_NAME = "genome_data.txt"; // Standardized filename
    // ---------------------

    /**
     * Generates a sequence of dynamic size using a fixed seed.
     * **Dynamic Alphabet:** The characters used for generation are derived directly from the provided pattern.
     * Injects the pattern at specific locations and saves the result to a text file.
     * * @param pattern The pattern to inject and derive the alphabet from.
     * @param size The total number of bases/characters to generate.
     * @return The absolute path to the generated file.
     * @throws IOException If file handling fails.
     */
    public static Path generateAndSaveFile(String pattern, int size) throws IOException {
        Path filePath = Path.of(FILE_NAME);

        // --- STEP 1: CLEANUP ---
        // Explicitly delete the old file (if it exists) to ensure we are generating a fresh file
        // every time, regardless of whether the pattern/size is the same or new.
        if (Files.exists(filePath)) {
            Files.delete(filePath);
            System.out.println("Old file deleted successfully: " + filePath.toAbsolutePath());
        } else {
            System.out.println("No old file found. Creating new file at: " + filePath.toAbsolutePath());
        }
        // -----------------------

        // --- STEP 2: PREPARE DYNAMIC ALPHABET ---
        // Convert pattern to uppercase to ensure consistency
        String p = pattern.toUpperCase();

        // Extract unique characters from the pattern to form the generation "alphabet"
        // e.g., Pattern "AGCT" -> Alphabet ['A', 'G', 'C', 'T']
        // e.g., Pattern "01"   -> Alphabet ['0', '1']
        String uniqueChars = p.chars()
                .distinct()
                .mapToObj(c -> String.valueOf((char) c))
                .collect(Collectors.joining());

        char[] dynamicBases = uniqueChars.toCharArray();

        if (dynamicBases.length == 0) {
            throw new IOException("Pattern must contain at least one character to generate a file.");
        }

        System.out.println("Generating " + size + " char sequence using alphabet " + uniqueChars + " from pattern: " + p);

        // Initialize Random with fixed seed for consistent benchmarking
        Random random = new Random(FIXED_SEED);

        // 3. Generate the sequence dynamically based on the extracted alphabet
        StringBuilder genome = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            genome.append(dynamicBases[random.nextInt(dynamicBases.length)]);
        }

        // 4. Inject known matches (Start, Middle, End)
        int pLen = p.length();
        int[] injectionPoints = {
                1000,                     // Near Start (if size allows)
                size / 2,                 // Exact Middle
                size - pLen - 1000        // Near End (if size allows)
        };

        for (int idx : injectionPoints) {
            // Bounds check to ensure we don't crash on very small files (< 1000 bases)
            if (idx >= 0 && idx + pLen <= size) {
                genome.replace(idx, idx + pLen, p);
            }
        }

        // 5. Write to file
        Files.writeString(filePath, genome.toString());

        System.out.println("File successfully generated and saved.");

        return filePath;
    }

    /**
     * Helper to read the file back if needed
     */
    public static String readGenomeFile() throws IOException {
        Path filePath = Path.of(FILE_NAME);
        if (!Files.exists(filePath)) {
            throw new IOException("Data file not found. Generate it first.");
        }
        return Files.readString(filePath);
    }
}