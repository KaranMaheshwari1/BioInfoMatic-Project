package com.bio.algorithm;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SuffixTrieNode {
    // Map to store child nodes, keyed by the next character in the suffix
    Map<Character, SuffixTrieNode> children = new HashMap<>(); 
    
    // Stores the starting index/indices of the suffix in the original text
    // A Set is used because multiple suffixes can share the same path
    Set<Integer> startIndices = new HashSet<>(); 
}