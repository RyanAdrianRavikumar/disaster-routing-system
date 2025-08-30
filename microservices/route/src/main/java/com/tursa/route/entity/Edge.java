package com.tursa.route.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "edges")
public class Edge {
    @Id
    @Column(name = "edge_id")
    private String edgeId;

    @Column(name = "from_node_id", nullable = false)
    private String fromNodeId;

    @Column(name = "to_node_id", nullable = false)
    private String toNodeId;

    @Column(name = "weight", nullable = false)
    private Double weight;

    @Column(name = "road_type")
    private String roadType;

    @Column(name = "is_bidirectional")
    private Boolean isBidirectional = true;

    @Column(name = "is_safe")
    private Boolean isSafe = true;

    // Constructors
    public Edge() {}

    public Edge(String fromNodeId, String toNodeId, Double weight) {
        this.fromNodeId = fromNodeId;
        this.toNodeId = toNodeId;
        this.weight = weight;
        this.isBidirectional = true;
        this.isSafe = true;
    }

    public Edge(String edgeId, String fromNodeId, String toNodeId, Double weight,
                String roadType, Boolean isBidirectional, Boolean isSafe) {
        this.edgeId = edgeId;
        this.fromNodeId = fromNodeId;
        this.toNodeId = toNodeId;
        this.weight = weight;
        this.roadType = roadType;
        this.isBidirectional = isBidirectional;
        this.isSafe = isSafe;
    }

    // Getters and Setters
    public String getEdgeId() { return edgeId; }
    public void setEdgeId(String edgeId) { this.edgeId = edgeId; }

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

    public Boolean getIsSafe() { return isSafe; }
    public void setIsSafe(Boolean isSafe) { this.isSafe = isSafe; }
}
