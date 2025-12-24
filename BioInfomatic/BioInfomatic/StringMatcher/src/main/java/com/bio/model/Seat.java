package com.bio.model;

public class Seat {
    private int seatId;
    private String name;
    private String cosmicFloor;
    private int cosmicFloorId;
    private String status; // "Available" or "Booked"

    // Constructor to easily initialize the hardcoded data
    public Seat(int seatId, String name, String cosmicFloor, int cosmicFloorId, String status) {
        this.seatId = seatId;
        this.name = name;
        this.cosmicFloor = cosmicFloor;
        this.cosmicFloorId = cosmicFloorId;
        this.status = status;
    }

    // --- Getters and Setters ---
    public int getSeatId() { return seatId; }
    public String getName() { return name; }
    public String getCosmicFloor() { return cosmicFloor; }
    public int getCosmicFloorId() { return cosmicFloorId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return name + " (" + cosmicFloor + ", Floor ID: " + cosmicFloorId + ")";
    }

    public void setName(String newName) {
    }
}