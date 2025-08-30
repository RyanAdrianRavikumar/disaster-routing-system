package com.tursa.route.repository;

import com.tursa.route.entity.Node;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NodeRepository extends JpaRepository<Node, String> {

    Optional<Node> findByNodeId(String nodeId);

    List<Node> findByIsSafeTrue();

    @Query("SELECT n FROM Node n ORDER BY (POWER(n.latitude - :lat, 2) + POWER(n.longitude - :lon, 2)) ASC LIMIT 1")
    Node findNearestNode(@Param("lat") Double latitude, @Param("lon") Double longitude);

    @Query("SELECT n FROM Node n WHERE " +
            "n.latitude BETWEEN :minLat AND :maxLat AND " +
            "n.longitude BETWEEN :minLon AND :maxLon")
    List<Node> findNodesInArea(@Param("minLat") Double minLat,
                               @Param("maxLat") Double maxLat,
                               @Param("minLon") Double minLon,
                               @Param("maxLon") Double maxLon);

    List<Node> findByNodeType(String nodeType);
}
