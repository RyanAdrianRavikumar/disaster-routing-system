package com.tursa.route.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "nodes")
public class Node {
    @Id
    @Column(name = "node_id")
    private String nodeId;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "node_name")
    private String nodeName;

    @Column(name = "node_type")
    private String nodeType;

    @Column(name = "is_safe")
    private Boolean isSafe = true;

    // Constructors
    public Node() {}

    public Node(String nodeId, Double latitude, Double longitude) {
        this.nodeId = nodeId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isSafe = true;
    }

    public Node(String nodeId, Double latitude, Double longitude,
                String nodeName, String nodeType, Boolean isSafe) {
        this.nodeId = nodeId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.nodeName = nodeName;
        this.nodeType = nodeType;
        this.isSafe = isSafe;
    }

    // Getters and Setters
    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getNodeName() { return nodeName; }
    public void setNodeName(String nodeName) { this.nodeName = nodeName; }

    public String getNodeType() { return nodeType; }
    public void setNodeType(String nodeType) { this.nodeType = nodeType; }

    public Boolean getIsSafe() { return isSafe; }
    public void setIsSafe(Boolean isSafe) { this.isSafe = isSafe; }
}