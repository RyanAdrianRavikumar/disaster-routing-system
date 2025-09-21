package com.tursa.shelterroute.entity;

public class Edge {
    private String from; // Added field for starting node
    private String to; // Added field for ending node
    private double weight; // Added field for edge weight (distance in km)
    private boolean blocked; // Added field for blocked status

    public Edge() {} // Added default constructor for Firebase

    public Edge(String from, String to, double weight, boolean blocked) { // Added constructor
        this.from = from;
        this.to = to;
        this.weight = weight;
        this.blocked = blocked;
    }

    // Getters
    public String getFrom() { return from; } // Added getter for from
    public String getTo() { return to; } // Added getter for to
    public double getWeight() { return weight; } // Added getter for weight
    public boolean isBlocked() { return blocked; } // Added getter for blocked

    // Setters
    public void setFrom(String from) { this.from = from; } // Added setter for from
    public void setTo(String to) { this.to = to; } // Added setter for to
    public void setWeight(double weight) { this.weight = weight; } // Added setter for weight
    public void setBlocked(boolean blocked) { this.blocked = blocked; } // Added setter for blocked
}