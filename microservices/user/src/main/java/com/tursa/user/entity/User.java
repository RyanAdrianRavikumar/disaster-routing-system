package com.tursa.user.entity;

import java.time.LocalDateTime;

public class User {

    private String id; // Firebase uses String IDs instead of Long
    private String rfid;
    private String name;
    private String email;
    private String password;
    private String phoneNumber;
    private Double currentLatitude;
    private Double currentLongitude;
    private Integer familyCount = 0;
    private Integer childrenCount = 0;
    private Integer elderlyCount = 0;
    private UserStatus status = UserStatus.SAFE;
    private Integer rescuePriority = 0;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Enum for user status
    public enum UserStatus {
        SAFE, EVACUATING, NEEDS_RESCUE
    }

    // Constructors
    public User() {}

    public User(String rfid, String name, String phoneNumber, String email, String password) {
        this.rfid = rfid;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.password = password;
        this.status = UserStatus.SAFE;
        this.rescuePriority = 0;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRfid() { return rfid; }
    public void setRfid(String rfid) { this.rfid = rfid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public Double getCurrentLatitude() { return currentLatitude; }
    public void setCurrentLatitude(Double currentLatitude) { this.currentLatitude = currentLatitude; }

    public Double getCurrentLongitude() { return currentLongitude; }
    public void setCurrentLongitude(Double currentLongitude) { this.currentLongitude = currentLongitude; }

    public Integer getFamilyCount() { return familyCount; }
    public void setFamilyCount(Integer familyCount) { this.familyCount = familyCount; }

    public Integer getChildrenCount() { return childrenCount; }
    public void setChildrenCount(Integer childrenCount) { this.childrenCount = childrenCount; }

    public Integer getElderlyCount() { return elderlyCount; }
    public void setElderlyCount(Integer elderlyCount) { this.elderlyCount = elderlyCount; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public Integer getRescuePriority() { return rescuePriority; }
    public void setRescuePriority(Integer rescuePriority) { this.rescuePriority = rescuePriority; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", rfid='" + rfid + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", currentLatitude=" + currentLatitude +
                ", currentLongitude=" + currentLongitude +
                ", familyCount=" + familyCount +
                ", childrenCount=" + childrenCount +
                ", elderlyCount=" + elderlyCount +
                ", status=" + status +
                ", rescuePriority=" + rescuePriority +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}