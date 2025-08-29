package com.tursa.route.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "route_history",
        indexes = {
                @Index(name = "idx_start_end", columnList = "start_node_id,end_node_id"),
                @Index(name = "idx_calculated_at", columnList = "calculated_at")
        })
public class RouteHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_node_id", nullable = false, length = 50)
    private String startNodeId;

    @Column(name = "end_node_id", nullable = false, length = 50)
    private String endNodeId;

    @Column(name = "route_path", columnDefinition = "TEXT")
    private String routePath; // JSON array of node IDs

    @Column(name = "total_distance", precision = 10, scale = 4)
    private Double totalDistance;

    @Column(name = "is_safe")
    private Boolean isSafe;

    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    public RouteHistory() {
        this.calculatedAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusHours(1); // Cache for 1 hour
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStartNodeId() { return startNodeId; }
    public void setStartNodeId(String startNodeId) { this.startNodeId = startNodeId; }

    public String getEndNodeId() { return endNodeId; }
    public void setEndNodeId(String endNodeId) { this.endNodeId = endNodeId; }

    public String getRoutePath() { return routePath; }
    public void setRoutePath(String routePath) { this.routePath = routePath; }

    public Double getTotalDistance() { return totalDistance; }
    public void setTotalDistance(Double totalDistance) { this.totalDistance = totalDistance; }

    public Boolean getIsSafe() { return isSafe; }
    public void setIsSafe(Boolean isSafe) { this.isSafe = isSafe; }

    public LocalDateTime getCalculatedAt() { return calculatedAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
}
