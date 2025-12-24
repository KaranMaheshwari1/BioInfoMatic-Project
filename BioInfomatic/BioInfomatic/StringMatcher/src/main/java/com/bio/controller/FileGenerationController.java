package com.bio.controller;

import com.bio.utils.GenomeDataGeneratorPro;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/bioinfo/data")
@CrossOrigin(origins = "*") // Allow requests from your frontend
public class FileGenerationController {

    @GetMapping("/generate-file/{pattern}")
    public ResponseEntity<String> generateFile(
            @PathVariable String pattern,
            @RequestParam(defaultValue = "1000000") int size) { // Accepting size from frontend

        if (pattern == null || pattern.isEmpty()) {
            return ResponseEntity.badRequest().body("Pattern is required.");
        }

        // Safety check to prevent server overload
        if (size <= 0 || size > 200_000_000) {
            return ResponseEntity.badRequest().body("Size must be between 1 and 200,000,000 bases.");
        }

        try {
            // Pass the dynamic size and pattern to the generator
            Path filePath = GenomeDataGeneratorPro.generateAndSaveFile(pattern, size);

            return ResponseEntity.ok("Successfully generated genome file (" + size + " bases): " + filePath.toAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to generate and save file: " + e.getMessage());
        }
    }
}