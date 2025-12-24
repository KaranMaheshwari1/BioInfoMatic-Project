package com.bio.algorithm;

import com.bio.model.MatchResult;
import org.biojava.nbio.core.sequence.DNASequence;
import org.biojava.nbio.core.sequence.compound.AmbiguityDNACompoundSet;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements sequence searching using BioJava for sequence parsing/validation
 * combined with Java's fast native RegEx engine for the actual matching.
 * This is the most stable and performant approach for substring search 
 * using the modern BioJava API without complex alignment objects.
 */
public class BioJavaSearch {

    public MatchResult search(String text, String pattern) throws Exception {

        // 1. Initialize BioJava objects to VALIDATE the input (A, C, G, T, etc.)
        AmbiguityDNACompoundSet compoundSet = AmbiguityDNACompoundSet.getDNACompoundSet();
        
        // Creating the sequence objects validates the sequence against the DNA alphabet. 
        // If characters like 'X' are included, BioJava might throw an exception (or handle it based on settings).
        DNASequence textSequence = new DNASequence(text, compoundSet);
        DNASequence patternSequence = new DNASequence(pattern, compoundSet);
        
        // Use the validated string representations for matching
        String searchText = textSequence.getSequenceAsString();
        String searchPattern = patternSequence.getSequenceAsString();

        List<Integer> matchIndices = new ArrayList<>();
        long startTime = System.nanoTime();

        // 2. Use Java's highly optimized Regular Expression engine for stable, fast matching.
        // We compile the pattern once.
        Pattern compiledPattern = Pattern.compile(searchPattern);
        Matcher matcher = compiledPattern.matcher(searchText);

        while (matcher.find()) {
            // matcher.start() returns the 0-based index of the match
            matchIndices.add(matcher.start());
        }

        long endTime = System.nanoTime();
        long executionTimeNs = endTime - startTime;

        return new MatchResult("BioJava/Regex Search", executionTimeNs, matchIndices, text.length(), pattern.length());
    }
}