package com.tursa.route.service;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class FirebaseRealtimeService {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseRealtimeService.class);

    private final DatabaseReference dbRef;

    public FirebaseRealtimeService() {
        this.dbRef = FirebaseDatabase.getInstance().getReference();
    }

    // Update route safety status in real-time
    public void updateRouteStatus(String routeId, boolean isSafe) {
        try {
            Map<String, Object> routeData = new HashMap<>();
            routeData.put("routeId", routeId);
            routeData.put("isSafe", isSafe);
            routeData.put("lastUpdated", LocalDateTime.now().toString());

            dbRef.child("routes").child(routeId).child("status").setValueAsync(routeData);

            logger.info("Updated route {} safety status to {} in Firebase", routeId, isSafe);
        } catch (Exception e) {
            logger.error("Error updating route status in Firebase: {}", e.getMessage());
        }
    }

    // Update route distance in real-time
    public void updateRouteDistance(String routeId, double distance) {
        try {
            Map<String, Object> distanceData = new HashMap<>();
            distanceData.put("distance", distance);
            distanceData.put("lastUpdated", LocalDateTime.now().toString());

            dbRef.child("routes").child(routeId).child("distance").setValueAsync(distanceData);

            logger.info("Updated route {} distance to {} km in Firebase", routeId, distance);
        } catch (Exception e) {
            logger.error("Error updating route distance in Firebase: {}", e.getMessage());
        }
    }

    // Update individual node safety status
    public void updateNodeSafety(String nodeId, boolean isSafe) {
        try {
            Map<String, Object> nodeData = new HashMap<>();
            nodeData.put("nodeId", nodeId);
            nodeData.put("isSafe", isSafe);
            nodeData.put("lastUpdated", LocalDateTime.now().toString());

            dbRef.child("nodes").child(nodeId).child("safety").setValueAsync(nodeData);

            logger.info("Updated node {} safety status to {} in Firebase", nodeId, isSafe);
        } catch (Exception e) {
            logger.error("Error updating node safety in Firebase: {}", e.getMessage());
        }
    }

    // Broadcast emergency alerts
    public void broadcastEmergencyAlert(String message) {
        try {
            Map<String, Object> alertData = new HashMap<>();
            alertData.put("message", message);
            alertData.put("timestamp", LocalDateTime.now().toString());
            alertData.put("type", "EMERGENCY");

            dbRef.child("alerts").push().setValueAsync(alertData);

            logger.warn("Broadcasted emergency alert: {}", message);
        } catch (Exception e) {
            logger.error("Error broadcasting emergency alert: {}", e.getMessage());
        }
    }

    // Broadcast route updates to affected users
    public void broadcastRouteUpdate(String startNodeId, String endNodeId, boolean newRouteSafety) {
        try {
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("startNode", startNodeId);
            updateData.put("endNode", endNodeId);
            updateData.put("isSafe", newRouteSafety);
            updateData.put("timestamp", LocalDateTime.now().toString());
            updateData.put("action", "ROUTE_RECALCULATED");

            dbRef.child("route_updates").push().setValueAsync(updateData);

            logger.info("Broadcasted route update for {} → {} - Safe: {}", startNodeId, endNodeId, newRouteSafety);
        } catch (Exception e) {
            logger.error("Error broadcasting route update: {}", e.getMessage());
        }
    }

    // Send route recommendations to users
    public void sendRouteRecommendation(String userRfid, String recommendedRoute, String reason) {
        try {
            Map<String, Object> recommendationData = new HashMap<>();
            recommendationData.put("userRfid", userRfid);
            recommendationData.put("recommendedRoute", recommendedRoute);
            recommendationData.put("reason", reason);
            recommendationData.put("timestamp", LocalDateTime.now().toString());

            dbRef.child("user_recommendations").child(userRfid).setValueAsync(recommendationData);

            logger.info("Sent route recommendation to user {}: {}", userRfid, recommendedRoute);
        } catch (Exception e) {
            logger.error("Error sending route recommendation: {}", e.getMessage());
        }
    }

    // Update real-time evacuation statistics
    public void updateEvacuationStats(int totalRoutes, int safeRoutes, int affectedNodes) {
        try {
            Map<String, Object> statsData = new HashMap<>();
            statsData.put("totalRoutes", totalRoutes);
            statsData.put("safeRoutes", safeRoutes);
            statsData.put("unsafeRoutes", totalRoutes - safeRoutes);
            statsData.put("affectedNodes", affectedNodes);
            statsData.put("lastUpdated", LocalDateTime.now().toString());

            dbRef.child("evacuation_stats").setValueAsync(statsData);

            logger.info("Updated evacuation statistics in Firebase");
        } catch (Exception e) {
            logger.error("Error updating evacuation stats: {}", e.getMessage());
        }
    }
}
