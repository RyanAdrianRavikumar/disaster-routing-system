package com.tursa.route.entity;

import java.util.List;

public class Route {
    private List<String> path;
    private List<Edge> edges;
    private Double totalDistance;
    private Boolean isSafe;

    // Constructors
    public Route() {}

    public Route(List<String> path, List<Edge> edges, Double totalDistance, Boolean isSafe) {
        this.path = path;
        this.edges = edges;
        this.totalDistance = totalDistance;
        this.isSafe = isSafe;
    }

    // Getters and Setters
    public List<String> getPath() { return path; }
    public void setPath(List<String> path) { this.path = path; }

    public Double getTotalDistance() { return totalDistance; }
    public void setTotalDistance(Double totalDistance) { this.totalDistance = totalDistance; }

    public Boolean isSafe() { return isSafe; }
    public void setIsSafe(Boolean isSafe) { this.isSafe = isSafe; }
}
