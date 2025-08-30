package com.tursa.rescue.dto;

public class UserDTO {
    private Long id;
    private String name;
    private Integer childrenCount;
    private Integer elderlyCount;

    // Getters/Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getChildrenCount() { return childrenCount; }
    public void setChildrenCount(Integer childrenCount) { this.childrenCount = childrenCount; }

    public Integer getElderlyCount() { return elderlyCount; }
    public void setElderlyCount(Integer elderlyCount) { this.elderlyCount = elderlyCount; }

    public int getPriority() {
        return (childrenCount != null ? childrenCount : 0) +
                (elderlyCount != null ? elderlyCount : 0);
    }
}