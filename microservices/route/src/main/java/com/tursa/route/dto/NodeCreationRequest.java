package com.tursa.route.dto;

public class NodeCreationRequest {
    private String nodeId;
    private Double latitude;
    private Double longitude;
    private String nodeName;
    private String nodeType;

    // Constructors
    public NodeCreationRequest() {}

    public NodeCreationRequest(String nodeId, Double latitude, Double longitude,
                               String nodeName, String nodeType) {
        this.nodeId = nodeId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.nodeName = nodeName;
        this.nodeType = nodeType;
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
}
