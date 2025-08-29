package com.tursa.route.dto;

public class EdgeCreationRequest {
    private String fromNodeId;
    private String toNodeId;
    private Double weight;
    private String roadType;
    private Boolean isBidirectional;

    // Constructors
    public EdgeCreationRequest() {}

    public EdgeCreationRequest(String fromNodeId, String toNodeId, Double weight,
                               String roadType, Boolean isBidirectional) {
        this.fromNodeId = fromNodeId;
        this.toNodeId = toNodeId;
        this.weight = weight;
        this.roadType = roadType;
        this.isBidirectional = isBidirectional;
    }

    // Getters and Setters
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
}