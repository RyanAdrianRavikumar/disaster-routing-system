package com.tursa.user.repository;

import com.tursa.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.rfid = :rfid")
    Optional<User> findByRfid(@Param("rfid") String rfid);

    @Query("SELECT u FROM User u WHERE u.status = :status ORDER BY u.rescuePriority DESC")
    List<User> findByStatusOrderByPriorityDesc(@Param("status") User.UserStatus status);

    @Query(value = "SELECT * FROM users WHERE " +
            "current_latitude BETWEEN :minLat AND :maxLat " +
            "AND current_longitude BETWEEN :minLon AND :maxLon",
            nativeQuery = true)
    List<User> findUsersInArea(@Param("minLat") double minLat, @Param("maxLat") double maxLat, @Param("minLon") double minLon, @Param("maxLon") double maxLon);

    @Modifying
    @Query("UPDATE User u SET u.currentLatitude = :latitude, u.currentLongitude = :longitude WHERE u.rfid = :rfid")
    int updateLocationByRfid(@Param("rfid") String rfid, @Param("latitude") Double latitude, @Param("longitude") Double longitude);
}
