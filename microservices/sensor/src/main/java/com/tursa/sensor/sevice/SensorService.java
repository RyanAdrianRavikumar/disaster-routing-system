package com.tursa.sensor.sevice;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.FirebaseApp;
import com.tursa.sensor.model.Obstacle;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class SensorService {

    private FirebaseDatabase firebaseDatabase;

    public SensorService() { }

    @PostConstruct
    private void initFirebase() {
        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                firebaseDatabase = FirebaseDatabase.getInstance();
                System.out.println("FirebaseDatabase instance ready");
            } else {
                System.out.println("FirebaseApp not initialized, skipping FirebaseDatabase");
            }
        } catch (Exception e) {
            System.err.println("FirebaseDatabase init failed: " + e.getMessage());
        }
    }

    public String recordSensorData(String sensorId, String data) {
        System.out.println("Recording sensor data: " + sensorId + " = " + data);

        if (firebaseDatabase != null) {
            // Parse the sensor data to extract obstacle information
            // Format: "edgeId:obstacleType:description"
            String[] parts = data.split(":");
            if (parts.length >= 3) {
                String edgeId = parts[0];
                String obstacleType = parts[1];
                String description = parts[2];

                // Create an obstacle object
                Obstacle obstacle = new Obstacle();
                obstacle.setId(sensorId);
                obstacle.setRoadSegment(edgeId);
                obstacle.setDescription(obstacleType + ": " + description);
                obstacle.setActive(true);

                // Save to Firebase under "obstacles" node
                DatabaseReference obstacleRef = firebaseDatabase.getReference("obstacles").child(sensorId);
                obstacleRef.setValueAsync(obstacle);

                // Update the corresponding edge to mark it as blocked
                updateEdgeBlockage(edgeId, true);

                return "Obstacle recorded and edge " + edgeId + " blocked: " + sensorId;
            } else {
                // If data format is incorrect, just save as generic obstacle
                Obstacle obstacle = new Obstacle();
                obstacle.setId(sensorId);
                obstacle.setRoadSegment("unknown");
                obstacle.setDescription(data);
                obstacle.setActive(true);

                DatabaseReference obstacleRef = firebaseDatabase.getReference("obstacles").child(sensorId);
                obstacleRef.setValueAsync(obstacle);

                return "Data recorded (generic obstacle): " + sensorId;
            }
        }

        return "Data recorded (no Firebase): " + sensorId;
    }

    private void updateEdgeBlockage(String edgeId, boolean blocked) {
        if (firebaseDatabase != null) {
            // Update the edge in Firebase
            DatabaseReference edgeRef = firebaseDatabase.getReference("edges").child(edgeId).child("blocked");
            edgeRef.setValueAsync(blocked);

            System.out.println("Updated edge " + edgeId + " blockage status to: " + blocked);
        }
    }

    // Method to clear an obstacle and unblock the edge
    public String clearObstacle(String sensorId) {
        if (firebaseDatabase != null) {
            // Get the obstacle to find which edge it was blocking
            DatabaseReference obstacleRef = firebaseDatabase.getReference("obstacles").child(sensorId);

            obstacleRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Obstacle obstacle = snapshot.getValue(Obstacle.class);
                        if (obstacle != null) {
                            // Unblock the edge
                            updateEdgeBlockage(obstacle.getRoadSegment(), false);

                            // Remove the obstacle
                            obstacleRef.removeValueAsync();

                            System.out.println("Cleared obstacle " + sensorId + " and unblocked edge " + obstacle.getRoadSegment());
                        }
                    }
                }

                @Override
                public void onCancelled(com.google.firebase.database.DatabaseError error) {
                    System.err.println("Failed to clear obstacle: " + error.getMessage());
                }
            });

            return "Clearing obstacle: " + sensorId;
        }

        return "Failed to clear obstacle: " + sensorId;
    }
}