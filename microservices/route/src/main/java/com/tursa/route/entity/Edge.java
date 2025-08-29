package com.tursa.route.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "graph_edges",
        indexes = {
                @Index(name = "idx_from_node", columnList = "from_node_id"),
                @Index(name = "idx_to_node", columnList = "to_node_id"),
                @Index(name = "idx_weight", columnList = "weight")
        })
public class Edge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_node_id", nullable = false, length = 50)
    private String fromNodeId;

    @Column(name = "to_node_id", nullable = false, length = 50)
    private String toNodeId;

    @Column(name = "weight", nullable = false, precision = 10, scale = 4)
    private Double weight; // Distance or time

    @Column(name = "road_type", length = 50) // HIGHWAY, MAIN_ROAD, SIDE_ROAD
    private String roadType;

    @Column(name = "is_bidirectional")
    private Boolean isBidirectional = true;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Constructors
    public Edge() {}

    public Edge(String fromNodeId, String toNodeId, Double weight) {
        this.fromNodeId = fromNodeId;
        this.toNodeId = toNodeId;
        this.weight = weight;
        this.isBidirectional = true;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFromNodeId() { return fromNodeId; }
    public void setFromNodeId(String fromNodeId) { this.fromNodeId = fromNodeId; }

    public String getToNodeId() { return toNodeId; }
    public void setToNodeId(String toNodeId) { this.toNodeId = toNodeId; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public String getRoadType() { return roadType; }
    public void setRoadType(String roadType) { this.roadType = roadType; }

    public Boolean getIsBidirectional() { return isBidirectional; }
    public void setIsBidirectional(Boolean isBidirectional) { this.isBidirectional = isBidirectional; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}