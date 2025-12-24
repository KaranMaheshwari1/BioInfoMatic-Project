package com.bio.model;

/**
 * Data Transfer Object (DTO) used to accept the Text, Pattern, and Algorithm name
 * from the React frontend's JSON POST request body.
 */
public class MatchRequest {

    // These field names MUST match the keys sent in the JSON body from the frontend.
    private String text;
    private String pattern;
    // ADDED FIELD: The name of the sequential base algorithm (e.g., "Naive")
    private String algorithm; 
    private int numChunks; // <--- NEW FIELD
    // --- Constructors ---
    // Default constructor is required by some Jackson JSON deserializers
    public MatchRequest() {
    }

    // Optional: All-arguments constructor (Updated)
    public MatchRequest(String text, String pattern, String algorithm) {
        this.text = text;
        this.pattern = pattern;
        this.algorithm = algorithm;
    }

    // --- Getters (Required by Spring/Jackson to read data) ---
    public String getText() {
        return text;
    }

    public String getPattern() {
        return pattern;
    }
    
    // Getter for the new field
    public String getAlgorithm() { 
        return algorithm;
    }

    // --- Setters (Required by Spring/Jackson to write data from JSON) ---
    public void setText(String text) {
        this.text = text;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
    
    // Setter for the new field
    public void setAlgorithm(String algorithm) { 
        this.algorithm = algorithm;
    }
 // Getters and Setters for all fields...

    public int getNumChunks() {
        return numChunks;
    }

    public void setNumChunks(int numChunks) {
        this.numChunks = numChunks;
    }
}