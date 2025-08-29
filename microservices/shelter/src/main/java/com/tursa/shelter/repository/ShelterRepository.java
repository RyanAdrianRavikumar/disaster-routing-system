package com.tursa.shelter.repository;

import com.tursa.shelter.model.Shelter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShelterRepository extends JpaRepository<Shelter, Long> {
    @Query("SELECT s FROM Shelter s WHERE s.isActive = true AND s.currentOccupancy < s.maxCapacity ORDER BY s.maxCapacity DESC")
    List<Shelter> findActiveSheltersWithCapacity();

    @Query(value = "SELECT *, " +
            "(6371 * ACOS(COS(RADIANS(:latitude)) * COS(RADIANS(latitude)) * " +
            "COS(RADIANS(longitude) - RADIANS(:longitude)) + " +
            "SIN(RADIANS(:latitude)) * SIN(RADIANS(latitude)))) AS distance " +
            "FROM shelters WHERE is_active = true AND current_occupancy < max_capacity " +
            "ORDER BY distance LIMIT 1",
            nativeQuery = true)
    Optional<Shelter> findNearestAvailableShelter(@Param("latitude") double latitude,
                                                  @Param("longitude") double longitude);

    @Modifying
    @Query("UPDATE Shelter s SET s.currentOccupancy = s.currentOccupancy + 1 WHERE s.id = :shelterId AND s.currentOccupancy < s.maxCapacity")
    int incrementOccupancy(@Param("shelterId") Long shelterId);

    @Modifying
    @Query("UPDATE Shelter s SET s.currentOccupancy = s.currentOccupancy - 1 WHERE s.id = :shelterId AND s.currentOccupancy > 0")
    int decrementOccupancy(@Param("shelterId") Long shelterId);
}