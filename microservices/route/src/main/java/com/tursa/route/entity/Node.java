package com.tursa.route.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "graph_nodes",
        indexes = {
                @Index(name = "idx_node_id", columnList = "node_id"),
                @Index(name = "idx_location", columnList = "latitude,longitude"),
                @Index(name = "idx_safe", columnList = "is_safe")
        })
public class Node {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "node_id", nullable = false, unique = true, length = 50)
    private String nodeId;

    @Column(name = "latitude", nullable = false, precision = 10, scale = 8)
    private Double latitude;

    @Column(name = "longitude", nullable = false, precision = 11, scale = 8)
    private Double longitude;

    @Column(name = "is_safe")
    private Boolean isSafe = true;

    @Column(name = "node_name", length = 100)
    private String nodeName;

    @Column(name = "node_type", length = 50) // INTERSECTION, SHELTER, HOSPITAL, etc.
    private String nodeType;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public Node() {}

    public Node(String nodeId, Double latitude, Double longitude) {
        this.nodeId = nodeId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isSafe = true;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Boolean getIsSafe() { return isSafe; }
    public void setIsSafe(Boolean isSafe) { this.isSafe = isSafe; }

    public String getNodeName() { return nodeName; }
    public void setNodeName(String nodeName) { this.nodeName = nodeName; }

    public String getNodeType() { return nodeType; }
    public void setNodeType(String nodeType) { this.nodeType = nodeType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}