package com.bio.dto;

public class OdooWebhookPayload {
    private int id;
    private String active_inactive;
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getActive_inactive() {
        return active_inactive;
    }

    public void setActive_inactive(String active_inactive) {
        this.active_inactive = active_inactive;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "OdooWebhookPayload{" +
                "id=" + id +
                ", active_inactive='" + active_inactive + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}