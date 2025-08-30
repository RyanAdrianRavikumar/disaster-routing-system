package com.tursa.route.repository;

import com.tursa.route.entity.RouteHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RouteHistoryRepository extends JpaRepository<RouteHistory, Long> {

    @Query("SELECT r FROM RouteHistory r WHERE " +
            "r.startNodeId = :startId AND r.endNodeId = :endId AND " +
            "r.expiresAt > :currentTime")
    Optional<RouteHistory> findValidRoute(@Param("startId") String startId,
                                          @Param("endId") String endId,
                                          @Param("currentTime") LocalDateTime currentTime);

    @Modifying
    @Query("DELETE FROM RouteHistory r WHERE r.expiresAt < :currentTime")
    int deleteExpiredRoutes(@Param("currentTime") LocalDateTime currentTime);
}
