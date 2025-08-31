package com.tursa.shelter.repository;

import com.google.firebase.database.*;
import com.tursa.shelter.entity.Shelter;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Repository
public class ShelterRepository {

    private final DatabaseReference dbRef;

    public ShelterRepository() {
        this.dbRef = FirebaseDatabase.getInstance().getReference("shelters");
    }

    // Save shelter asynchronously with completion future
    public CompletableFuture<Void> saveShelter(Shelter shelter) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        dbRef.child(shelter.getShelterId()).setValue(shelter, (error, ref) -> {
            if (error != null) {
                future.completeExceptionally(new RuntimeException("Firebase save error: " + error.getMessage()));
            } else {
                future.complete(null);
            }
        });
        return future;
    }

    // Async fetch all shelters
    public CompletableFuture<List<Shelter>> getAllSheltersFromFirebase() {
        CompletableFuture<List<Shelter>> future = new CompletableFuture<>();
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Shelter> shelters = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Shelter s = child.getValue(Shelter.class);
                    if (s != null) shelters.add(s);
                }
                future.complete(shelters);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(new RuntimeException("Firebase fetch error: " + error.getMessage()));
            }
        });
        return future;
    }

    // Async fetch single shelter by ID
    public CompletableFuture<Shelter> getShelterFromFirebase(String shelterId) {
        CompletableFuture<Shelter> future = new CompletableFuture<>();
        dbRef.child(shelterId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Shelter shelter = snapshot.getValue(Shelter.class);
                future.complete(shelter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(new RuntimeException("Firebase fetch error: " + error.getMessage()));
            }
        });
        return future;
    }
}