package com.tursa.shelterroute.entity;

public class Edge {
    private String from; // Starting node
    private String to; // Ending node
    private double weight; // Edge weight (distance in km)
    private boolean blocked; // Blocked status

    public Edge() {} // Default constructor for Firebase

    public Edge(String from, String to, double weight, boolean blocked) {
        this.from = from;
        this.to = to;
        this.weight = weight;
        this.blocked = blocked;
    }

    // Getters
    public String getFrom() { return from; }
    public String getTo() { return to; }
    public double getWeight() { return weight; }
    public boolean isBlocked() { return blocked; }

    // Setters
    public void setFrom(String from) { this.from = from; }
    public void setTo(String to) { this.to = to; }
    public void setWeight(double weight) { this.weight = weight; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
}