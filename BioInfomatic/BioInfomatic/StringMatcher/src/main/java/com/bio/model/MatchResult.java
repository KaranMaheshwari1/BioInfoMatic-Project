package com.bio.model;

import java.util.ArrayList;
import java.util.List;

public class MatchResult {

    private int availableCores;
    private String cpuArchitecture; // e.g., "amd64" or "aarch64"
    private String operatingSystem; // e.g., "Windows 10" or "Linux"
    private long totalMemoryMB;
    // --- Core Fields ---
    private String algorithm;
    private long executionTimeNs;
    private List<Integer> matchIndices;
    private int textLength;
    private int patternLength;
    private String errorMessage;
    
    // --- NEW ANALYSIS FIELDS (Required by Improvise and SuffixTree) ---
    private long overheadTimeNs;    // Time spent on preprocessing/construction (e.g., Suffix Tree build)
    private long comparisonCount;   // Total character comparisons made

    // --- FIX 1: Default (No-Argument) Constructor ---
    // Required for Spring/Jackson deserialization and simple object creation (new MatchResult())
    public MatchResult() {
        this.matchIndices = new ArrayList<>();
    }
    
    // --- FIX 2: Constructor for Error Responses ---
    // Used by Controllers for handling bad requests or internal errors
    public MatchResult(String errorMessage) {
        this(); // Call default constructor
        this.errorMessage = errorMessage;
    }

    // --- FIX 3: Constructor for Simple NaiveSearch (5 Arguments) ---
    // Usage: return new MatchResult(String algorithm, long executionTimeNs, List<Integer> matchIndices, int N, int M)
    public MatchResult(String algorithm, long executionTimeNs, List<Integer> matchIndices, int textLength, int patternLength) {
        this(); // Call default constructor
        this.algorithm = algorithm;
        this.executionTimeNs = executionTimeNs;
        this.matchIndices = matchIndices;
        this.textLength = textLength;
        this.patternLength = patternLength;
    }
    
    // --- FIX 4: Constructor for Improvised NaiveSearch (6 Arguments with Comparison Count) ---
    // Usage: return new MatchResult(String algorithm, long executionTimeNs, List<Integer> matchIndices, int N, int M, long comparisonCount)
    public MatchResult(String algorithm, long executionTimeNs, List<Integer> matchIndices, int textLength, int patternLength, long comparisonCount) {
        this(algorithm, executionTimeNs, matchIndices, textLength, patternLength);
        this.comparisonCount = comparisonCount;
    }

    // --- FIX 5: Constructor for SuffixTreeSearch (7 Arguments with Overhead and Comparisons) ---
    // Usage: return new MatchResult(String algorithm, long totalTime, long constructionTime, List<Integer> matches, int N, int M, long comparisonCount)
    public MatchResult(String algorithm, long executionTimeNs, long overheadTimeNs, List<Integer> matchIndices, int textLength, int patternLength, long comparisonCount) {
        this(algorithm, executionTimeNs, matchIndices, textLength, patternLength, comparisonCount);
        this.overheadTimeNs = overheadTimeNs;
    }
    

    // --- Getters and Setters ---
    
    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public long getExecutionTimeNs() {
        return executionTimeNs;
    }

    public void setExecutionTimeNs(long executionTimeNs) {
        this.executionTimeNs = executionTimeNs;
    }

    public List<Integer> getMatchIndices() {
        return matchIndices;
    }

    public void setMatchIndices(List<Integer> matchIndices) {
        this.matchIndices = matchIndices;
    }
    
    public int getTextLength() {
        return textLength;
    }

    public void setTextLength(int textLength) {
        this.textLength = textLength;
    }

    public int getPatternLength() {
        return patternLength;
    }

    public void setPatternLength(int patternLength) {
        this.patternLength = patternLength;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    // New Getters/Setters for Analysis Fields
    public long getOverheadTimeNs() {
        return overheadTimeNs;
    }

    public void setOverheadTimeNs(long overheadTimeNs) {
        this.overheadTimeNs = overheadTimeNs;
    }

    public long getComparisonCount() {
        return comparisonCount;
    }

    public void setComparisonCount(long comparisonCount) {
        this.comparisonCount = comparisonCount;
    }
    public int getAvailableCores() {
        return availableCores;
    }

    public void setAvailableCores(int availableCores) {
        this.availableCores = availableCores;
    }
    public String getCpuArchitecture() { return cpuArchitecture; }
    public void setCpuArchitecture(String cpuArchitecture) { this.cpuArchitecture = cpuArchitecture; }

    public String getOperatingSystem() { return operatingSystem; }
    public void setOperatingSystem(String operatingSystem) { this.operatingSystem = operatingSystem; }

    public long getTotalMemoryMB() { return totalMemoryMB; }
    public void setTotalMemoryMB(long totalMemoryMB) { this.totalMemoryMB = totalMemoryMB; }
}
