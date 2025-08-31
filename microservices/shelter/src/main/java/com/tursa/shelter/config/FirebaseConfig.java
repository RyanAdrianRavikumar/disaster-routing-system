package com.tursa.shelter.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;

@Configuration
public class FirebaseConfig {
    @PostConstruct
    public void initFirebase() {
        try {
            FileInputStream serviceAccount =
                    new FileInputStream("src/main/resources/smart-evacuation-system-firebase-adminsdk-fbsvc-c2b2b3872d.json");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://smart-evacuation-system-default-rtdb.asia-southeast1.firebasedatabase.app/") // replace with your Firebase DB URL
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

            System.out.println("Firebase initialized");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }
}
