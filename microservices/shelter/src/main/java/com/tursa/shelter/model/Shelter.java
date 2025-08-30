package com.tursa.shelter.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.Id;
import java.time.LocalDateTime;

@Entity
@Table(name = "shelters",
        indexes = {
                @Index(name = "idx_active", columnList = "is_active"),
                @Index(name = "idx_location", columnList = "latitude,longitude"),
                @Index(name = "idx_capacity", columnList = "current_occupancy,max_capacity")
        })
public class Shelter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "latitude", nullable = false, precision = 10, scale = 8)
    private Double latitude;

    @Column(name = "longitude", nullable = false, precision = 11, scale = 8)
    private Double longitude;

    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity;

    @Column(name = "current_occupancy")
    private Integer currentOccupancy = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "facilities", columnDefinition = "TEXT")
    private String facilities;

    @Column(name = "contact_number", length = 20)
    private String contactNumber;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public Shelter() {}

    public Shelter(String name, Double latitude, Double longitude, Integer maxCapacity) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.maxCapacity = maxCapacity;
        this.currentOccupancy = 0;
        this.isActive = true;
    }

    // Helper methods
    public boolean hasCapacity() {
        return currentOccupancy < maxCapacity;
    }

    public Integer getAvailableCapacity() {
        return maxCapacity - currentOccupancy;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Integer getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(Integer maxCapacity) { this.maxCapacity = maxCapacity; }

    public Integer getCurrentOccupancy() { return currentOccupancy; }
    public void setCurrentOccupancy(Integer currentOccupancy) { this.currentOccupancy = currentOccupancy; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public String getFacilities() { return facilities; }
    public void setFacilities(String facilities) { this.facilities = facilities; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}