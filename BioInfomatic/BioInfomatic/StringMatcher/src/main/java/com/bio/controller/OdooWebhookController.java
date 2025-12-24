package com.bio.controller;

import com.bio.dto.OdooWebhookPayload;
import com.bio.service.FileUpdateService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/odoo")
public class OdooWebhookController {

    private final FileUpdateService fileUpdateService;

    public OdooWebhookController(FileUpdateService fileUpdateService) {
        this.fileUpdateService = fileUpdateService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleOdooWebhook(@RequestBody OdooWebhookPayload payload) {
        try {
            System.out.println("FULL PAYLOAD OBJECT:\n" + payload);
            System.out.println("Seat Name: " + payload.getName());
            System.out.println("Status: " + payload.getActive_inactive());
            int seatId = payload.getId();
            String status = payload.getActive_inactive();
            String seatName = payload.getName();

            String contentToAppend = formatDataForFile(payload);
            fileUpdateService.updateAndSaveScript(seatId, status, seatName);
            return ResponseEntity.ok("Webhook data processed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Error processing webhook data: " + e.getMessage());
        }
    }

    private String formatDataForFile(OdooWebhookPayload payload) {
        return String.format(
                "\n# Odoo Webhook Update\n" +
                        "Record ID: %d\n" +
                        "Name: %s\n" +
                        "Status: %s\n",

                payload.getId(),
                payload.getName(),
                payload.getActive_inactive()
        );
    }
    @GetMapping(value = "/seats-status", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getSeatStatusFile() {
        try {
            // The service method now reads and returns the file content
            String fileContent = fileUpdateService.readScriptContent();
            return ResponseEntity.ok(fileContent);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Error reading seat status file: " + e.getMessage());
        }
    }

}
