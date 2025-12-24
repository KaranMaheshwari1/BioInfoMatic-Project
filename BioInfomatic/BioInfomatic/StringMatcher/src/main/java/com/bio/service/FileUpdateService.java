package com.bio.service;

import com.bio.model.Seat;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class FileUpdateService {

    private static final String FILE_PATH = "logs/odoo_webhook_updates.txt";
    private static final String ODOO_BOOKED_STATUS = "booked";
    private static final String FILE_STATUS_BOOKED = "Booked";
    private static final String FILE_STATUS_AVAILABLE = "Available";

    // Define the marker that separates static content from dynamic content (MUST be unique)
    private static final String STATIC_END_MARKER = "Dedicated power backup and air conditioning\n";

    // NEW MARKER: Defines the start of the parsable seat list block (inside the dynamic section)
    private static final String DYNAMIC_LIST_START_MARKER = "here is list of available seats:\n";

    // --- Core Methods ---

    public void updateAndSaveScript(int seatId, String status, String seatName) throws IOException {

        // 1. Read entire file content
        Path path = Paths.get(FILE_PATH);
        if (!Files.exists(path)) {
            throw new IOException("Cannot update file: " + FILE_PATH + " does not exist.");
        }
        String originalContent = Files.readString(path);


        // 2. Read and Parse Inventory from the file's dynamic section
        List<Seat> currentInventory = readAndParseInventory(originalContent);

        if (currentInventory.isEmpty()) {
            System.err.println("Warning: File inventory is empty or could not be parsed. Cannot update.");
            return;
        }

        // 3. Update Transient Inventory Status
        String newFileStatus = status.equalsIgnoreCase(ODOO_BOOKED_STATUS) ? FILE_STATUS_BOOKED : FILE_STATUS_AVAILABLE;
        updateTransientInventoryStatus(currentInventory, seatId, newFileStatus, seatName);

        // 4. Generate New Dynamic Output Content
        String newDynamicSeatSection = generateDynamicSeatSection(currentInventory);

        // 5. Combine Static and Dynamic Content (Uses the fixed logic)
        String fullNewContent = combineStaticAndDynamicContent(originalContent, newDynamicSeatSection);

        // 6. Write to File
        Files.writeString(path, fullNewContent);

        System.out.println("Successfully updated file for Seat ID " + seatId + ". Status set to: " + newFileStatus);
    }

// ----------------------------------------------------------------------------------

    /**
     * Extracts the full inventory from the provided content string.
     */
    private List<Seat> readAndParseInventory(String content) {
        List<Seat> inventory = new ArrayList<>();

        // 1. Regex to find the entire dynamic section starting AFTER the static marker
        Pattern dynamicSectionPattern = Pattern.compile(
                Pattern.quote(STATIC_END_MARKER) + "\\s*(.*)", Pattern.DOTALL);
        Matcher dynamicMatcher = dynamicSectionPattern.matcher(content);

        String dynamicContent;
        if (dynamicMatcher.find()) {
            dynamicContent = dynamicMatcher.group(1);
        } else {
            System.err.println("Error: Static content marker not found. Cannot parse dynamic section.");
            return inventory;
        }

        // 2. Regex to find each floor section and its seat list
        // It looks for the "How many seats are available in [FloorName]?" header and captures the entire block
        // until the next floor header or the end of the file.
        Pattern floorSectionPattern = Pattern.compile(
                "(How many seats are available in .*?\\?\\s*.*?)\\s*" + // Capture the static/preamble text (Group 1)
                        Pattern.quote(DYNAMIC_LIST_START_MARKER) + "\\s*(.*?)" + // Capture the seat list block (Group 2)
                        "(?=\\s*How many seats are available in|$)", // Lookahead for next header or EOF
                Pattern.DOTALL);

        Matcher floorMatcher = floorSectionPattern.matcher(dynamicContent);

        // 3. Regex to find seat details: - Seat Name (ID: 19) (Status: Available)
        Pattern seatPattern = Pattern.compile("- (.*?) \\(ID: (\\d+)\\) \\(Status: (.*?)\\)");

        while (floorMatcher.find()) {
            // Extract the floor name from the preamble (Group 1) for the Seat object
            String preamble = floorMatcher.group(1).trim();
            Pattern floorNamePattern = Pattern.compile("How many seats are available in (.*?)\\?");
            Matcher nameMatcher = floorNamePattern.matcher(preamble);
            String cosmicFloor = nameMatcher.find() ? nameMatcher.group(1).trim() : "Unknown Floor";

            // Capture the block of text containing the seat list (Group 2)
            String seatListBlock = floorMatcher.group(2).trim();

            Matcher seatMatcher = seatPattern.matcher(seatListBlock);

            while (seatMatcher.find()) {
                String name = seatMatcher.group(1).trim();
                int id = Integer.parseInt(seatMatcher.group(2).trim());
                String status = seatMatcher.group(3).trim();

                inventory.add(new Seat(id, name, cosmicFloor, 0, status));
            }
        }
        return inventory;
    }


    private void updateTransientInventoryStatus(List<Seat> inventory, int seatId, String newStatus, String newName) {
        inventory.stream()
                .filter(seat -> seat.getSeatId() == seatId)
                .findFirst()
                .ifPresentOrElse(seat -> {

                    if (!seat.getStatus().equalsIgnoreCase(newStatus)) {
                        seat.setStatus(newStatus);
                        // Update the seat name
                        seat.setName(newName);
                        System.out.println("Seat ID " + seatId + " status updated to: " + newStatus);
                    } else {
                        System.out.println("Seat ID " + seatId + " status is already: " + newStatus + ". No change made.");
                    }
                }, () -> {
                    System.err.println("Error: Seat ID " + seatId + " not found in the parsed inventory. Update failed.");
                });
    }

    private String generateDynamicSeatSection(List<Seat> inventory) {
        StringBuilder sb = new StringBuilder();

        Map<String, List<Seat>> seatsByFloor = inventory.stream()
                .collect(Collectors.groupingBy(Seat::getCosmicFloor));

        // Sort keys (floor names) to ensure consistent file order
        List<String> sortedFloorNames = seatsByFloor.keySet().stream().sorted().collect(Collectors.toList());

        for (String floorName : sortedFloorNames) {
            List<Seat> floorSeats = seatsByFloor.get(floorName);

            long availableCount = floorSeats.stream()
                    .filter(seat -> seat.getStatus().equalsIgnoreCase(FILE_STATUS_AVAILABLE))
                    .count();

            long bookedCount = floorSeats.stream()
                    .filter(seat -> seat.getStatus().equalsIgnoreCase(FILE_STATUS_BOOKED))
                    .count();

            // --- GENERATE NEW FLOOR SECTION PREAMBLE (Static Text per Floor) ---

            sb.append("\n\n")
                    .append("How many seats are available in ").append(floorName).append("?\n")
                    .append(floorName).append(" currently offers a limited number of available seats. Availability may vary depending on the workspace type you prefer. Please contact our team or visit our booking page to check real-time seat availability and reserve your spot.\n")
                    .append("so there are **").append(availableCount).append("** seats available and **").append(bookedCount).append("** seats booked in ").append(floorName).append(".\n")
                    .append("Below is the list of all seats and their current status.\n")

                    // The DYNAMIC_LIST_START_MARKER must be here to separate preamble from the list
                    .append(DYNAMIC_LIST_START_MARKER);

            // List of ALL Seats
            floorSeats.forEach(seat -> {
                // Generate the exact format needed for future parsing
                sb.append("- ").append(seat.getName()).append(" (ID: ").append(seat.getSeatId()).append(") (Status: ").append(seat.getStatus()).append(")\n");
            });

        }
        return sb.toString();
    }

    /**
     * FIX: Splits the original file content at the STATIC_END_MARKER and replaces everything
     * that follows with the newly generated dynamic section.
     * The fix is achieved by generating the *entire* floor section preamble (including headers
     * and descriptive text) inside generateDynamicSeatSection().
     */
    private String combineStaticAndDynamicContent(String originalContent, String newDynamicSeatSection) {
        int markerIndex = originalContent.indexOf(STATIC_END_MARKER);

        if (markerIndex == -1) {
            System.err.println("Warning: Static content marker not found. Appending new content.");
            return originalContent + "\n" + newDynamicSeatSection;
        }

        // The index of the character AFTER the static marker (where the dynamic content begins)
        int dynamicStartIndex = markerIndex + STATIC_END_MARKER.length();

        // 1. Get the preserved static header (including the marker itself)
        String staticHeader = originalContent.substring(0, dynamicStartIndex);

        // 2. Combine the static header with the new, fully-formed dynamic content.
        // The new dynamic content includes the entire floor sections, headers, and seat lists.
        return staticHeader + newDynamicSeatSection;
    }


    public String readScriptContent() throws IOException {
        Path path = Paths.get(FILE_PATH);
        if (!Files.exists(path)) {
            throw new IOException("Cannot read file: " + FILE_PATH + " does not exist.");
        }
        return Files.readString(path);
    }
}