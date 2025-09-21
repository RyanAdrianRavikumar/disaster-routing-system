package com.tursa.shelterroute.entity;

public class Node {
    private String id; // Added field for node ID
    private String name; // Added field for node name
    private Double latitude; // Added field for latitude
    private Double longitude; // Added field for longitude

    public Node() {} // Added default constructor for Firebase

    public Node(String id, String name, Double latitude, Double longitude) { // Added constructor
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters
    public String getId() { return id; } // Added getter for id
    public String getName() { return name; } // Added getter for name
    public Double getLatitude() { return latitude; } // Added getter for latitude
    public Double getLongitude() { return longitude; } // Added getter for longitude

    // Setters
    public void setId(String id) { this.id = id; } // Added setter for id
    public void setName(String name) { this.name = name; } // Added setter for name
    public void setLatitude(Double latitude) { this.latitude = latitude; } // Added setter for latitude
    public void setLongitude(Double longitude) { this.longitude = longitude; } // Added setter for longitude
}