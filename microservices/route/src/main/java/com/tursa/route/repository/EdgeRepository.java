package com.tursa.route.repository;

import com.tursa.route.entity.Edge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EdgeRepository extends JpaRepository<Edge, String> {

    List<Edge> findByFromNodeIdAndIsSafeTrue(String fromNodeId);

    List<Edge> findByToNodeIdAndIsSafeTrue(String toNodeId);

    @Query("SELECT e FROM Edge e WHERE e.isSafe = true")
    List<Edge> findAllSafeEdges();

    @Query("SELECT e FROM Edge e WHERE " +
            "(e.fromNodeId = :nodeId OR e.toNodeId = :nodeId) AND e.isSafe = true")
    List<Edge> findEdgesConnectedToNode(@Param("nodeId") String nodeId);

    Optional<Edge> findByFromNodeIdAndToNodeId(String fromNodeId, String toNodeId);
}
