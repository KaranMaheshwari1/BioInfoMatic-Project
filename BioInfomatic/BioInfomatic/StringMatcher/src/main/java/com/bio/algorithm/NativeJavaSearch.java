package com.bio.algorithm;


import java.util.ArrayList;
import java.util.List;

import com.bio.model.MatchResult;

public class NativeJavaSearch {

 public MatchResult search(String text, String pattern) {
     List<Integer> matchIndices = new ArrayList<>();
     int index = -1;
     long startTime = System.nanoTime();

     // Use the native, highly optimized String.indexOf() method
     do {
         // Start search from the position after the last match
         index = text.indexOf(pattern, index + 1); 
         if (index != -1) {
             matchIndices.add(index);
         }
     } while (index != -1);

     long endTime = System.nanoTime();
     long executionTimeNs = endTime - startTime;

     return new MatchResult("Native Java indexOf()", executionTimeNs, matchIndices, text.length(), pattern.length());
 }
}