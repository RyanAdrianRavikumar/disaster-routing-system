package com.tursa.user.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.tursa.user.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final String COLLECTION_NAME = "users";

    private Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }

    public List<User> getAllUsers() throws InterruptedException, ExecutionException {
        Firestore db = getFirestore();
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        List<User> users = new ArrayList<>();
        for (QueryDocumentSnapshot document : documents) {
            User user = document.toObject(User.class);
            user.setId(document.getId());
            users.add(user);
        }
        return users;
    }

    public User createUser(User user) throws InterruptedException, ExecutionException {
        Firestore db = getFirestore();

        // Check if RFID already exists
        Query query = db.collection(COLLECTION_NAME).whereEqualTo("rfid", user.getRfid());
        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        if (!querySnapshot.get().getDocuments().isEmpty()) {
            throw new IllegalArgumentException("User with RFID " + user.getRfid() + " already exists");
        }

        DocumentReference docRef = db.collection(COLLECTION_NAME).document();
        user.setId(docRef.getId());
        ApiFuture<WriteResult> result = docRef.set(user);
        result.get(); // Wait for the operation to complete

        logger.info("Created new user in Firebase: {}", user.getRfid());
        return user;
    }

    public User findByRfid(String rfid) throws InterruptedException, ExecutionException {
        Firestore db = getFirestore();
        Query query = db.collection(COLLECTION_NAME).whereEqualTo("rfid", rfid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();
        if (documents.isEmpty()) {
            return null;
        }

        User user = documents.get(0).toObject(User.class);
        user.setId(documents.get(0).getId());
        return user;
    }

    public User updateLocation(String rfid, Double latitude, Double longitude) throws InterruptedException, ExecutionException {
        Firestore db = getFirestore();
        Query query = db.collection(COLLECTION_NAME).whereEqualTo("rfid", rfid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();
        if (documents.isEmpty()) {
            return null;
        }

        DocumentReference docRef = documents.get(0).getReference();
        ApiFuture<WriteResult> result = docRef.update(
                "currentLatitude", latitude,
                "currentLongitude", longitude
        );
        result.get();

        User user = documents.get(0).toObject(User.class);
        user.setId(documents.get(0).getId());
        user.setCurrentLatitude(latitude);
        user.setCurrentLongitude(longitude);

        logger.info("Updated location for user: {} to ({}, {})", rfid, latitude, longitude);
        return user;
    }

    public List<User> getUsersNeedingRescue() throws InterruptedException, ExecutionException {
        Firestore db = getFirestore();
        Query query = db.collection(COLLECTION_NAME)
                .whereEqualTo("status", User.UserStatus.NEEDS_RESCUE.toString())
                .orderBy("rescuePriority", Query.Direction.DESCENDING);

        ApiFuture<QuerySnapshot> future = query.get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        List<User> users = new ArrayList<>();
        for (QueryDocumentSnapshot document : documents) {
            User user = document.toObject(User.class);
            user.setId(document.getId());
            users.add(user);
        }
        return users;
    }

    public User updateUserStatus(String rfid, User.UserStatus status) throws InterruptedException, ExecutionException {
        Firestore db = getFirestore();
        Query query = db.collection(COLLECTION_NAME).whereEqualTo("rfid", rfid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();
        if (documents.isEmpty()) {
            return null;
        }

        DocumentReference docRef = documents.get(0).getReference();
        ApiFuture<WriteResult> result = docRef.update("status", status.toString());
        result.get();

        // If status is NEEDS_RESCUE, calculate and update priority
        if (status == User.UserStatus.NEEDS_RESCUE) {
            User user = documents.get(0).toObject(User.class);
            int priority = calculateRescuePriority(user);
            docRef.update("rescuePriority", priority).get();
        }

        User user = documents.get(0).toObject(User.class);
        user.setId(documents.get(0).getId());
        user.setStatus(status);

        logger.info("Updated status for user {} to {}", rfid, status);
        return user;
    }

    public List<User> getUsersInArea(Double latitude, Double longitude, Double radiusKm) throws InterruptedException, ExecutionException {
        Firestore db = getFirestore();

        // Calculate approximate bounding box
        double latRange = radiusKm / 111.0;
        double lonRange = radiusKm / (111.0 * Math.cos(Math.toRadians(latitude)));

        double minLat = latitude - latRange;
        double maxLat = latitude + latRange;
        double minLon = longitude - lonRange;
        double maxLon = longitude + lonRange;

        Query query = db.collection(COLLECTION_NAME)
                .whereGreaterThanOrEqualTo("currentLatitude", minLat)
                .whereLessThanOrEqualTo("currentLatitude", maxLat)
                .whereGreaterThanOrEqualTo("currentLongitude", minLon)
                .whereLessThanOrEqualTo("currentLongitude", maxLon);

        ApiFuture<QuerySnapshot> future = query.get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        List<User> users = new ArrayList<>();
        for (QueryDocumentSnapshot document : documents) {
            User user = document.toObject(User.class);
            user.setId(document.getId());

            // Calculate actual distance
            double distance = calculateDistance(latitude, longitude,
                    user.getCurrentLatitude(), user.getCurrentLongitude());

            if (distance <= radiusKm) {
                users.add(user);
            }
        }
        return users;
    }

    public User updateFamilyInfo(String rfid, Integer familyCount, Integer childrenCount, Integer elderlyCount) throws InterruptedException, ExecutionException {
        Firestore db = getFirestore();
        Query query = db.collection(COLLECTION_NAME).whereEqualTo("rfid", rfid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();

        List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();
        if (documents.isEmpty()) {
            return null;
        }

        DocumentReference docRef = documents.get(0).getReference();
        ApiFuture<WriteResult> result = docRef.update(
                "familyCount", familyCount,
                "childrenCount", childrenCount,
                "elderlyCount", elderlyCount
        );
        result.get();

        // If user needs rescue, recalculate priority
        User currentUser = documents.get(0).toObject(User.class);
        if (currentUser.getStatus() == User.UserStatus.NEEDS_RESCUE) {
            int priority = calculateRescuePriority(currentUser);
            docRef.update("rescuePriority", priority).get();
        }

        User user = documents.get(0).toObject(User.class);
        user.setId(documents.get(0).getId());
        user.setFamilyCount(familyCount);
        user.setChildrenCount(childrenCount);
        user.setElderlyCount(elderlyCount);

        return user;
    }

    private double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lat2 == null || lon1 == null || lon2 == null) {
            return Double.MAX_VALUE;
        }

        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Distance in km
    }

    private int calculateRescuePriority(User user) {
        int priority = 1; // Base priority

        // Higher priority for vulnerable populations
        if (user.getChildrenCount() != null) {
            priority += user.getChildrenCount() * 3;
        }
        if (user.getElderlyCount() != null) {
            priority += user.getElderlyCount() * 2;
        }
        if (user.getFamilyCount() != null) {
            priority += Math.max(0, user.getFamilyCount() - 2); // Extra points for larger families
        }

        return Math.min(priority, 10); // Cap at 10
    }
}