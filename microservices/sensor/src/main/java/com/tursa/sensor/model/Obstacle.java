package com.tursa.sensor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Obstacle {
    private String id;
    private String roadSegment;
    private String description;
    private boolean active; // true = obstacle exists

    // Optional: explicit getters and setters
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getRoadSegment() {
        return roadSegment;
    }
    public void setRoadSegment(String roadSegment) {
        this.roadSegment = roadSegment;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }
}
