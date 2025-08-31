package com.tursa.shelter.repository;

import com.google.firebase.database.*;
import com.tursa.shelter.entity.Shelter;
import org.springframework.stereotype.Repository;

import java.util.concurrent.CompletableFuture;

@Repository
public class ShelterRepository {

    private final DatabaseReference dbRef;

    public ShelterRepository() {
        this.dbRef = FirebaseDatabase.getInstance().getReference("shelters");
    }

    public void saveShelter(Shelter shelter) {
        dbRef.child(shelter.getShelterId()).setValueAsync(shelter);
    }

    public CompletableFuture<Shelter> getShelter(String shelterId) {
        CompletableFuture<Shelter> future = new CompletableFuture<>();
        dbRef.child(shelterId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Shelter shelter = snapshot.getValue(Shelter.class);
                future.complete(shelter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(error.toException());
            }
        });
        return future;
    }
}
