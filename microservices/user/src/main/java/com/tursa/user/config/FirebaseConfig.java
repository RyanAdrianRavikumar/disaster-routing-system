package com.tursa.user.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import jakarta.annotation.PostConstruct;

import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initFirebase() {
        try {
            // Load the service account JSON from the classpath
            ClassPathResource resource = new ClassPathResource("disasterhub-fbe9c-firebase-adminsdk-fbsvc-ad7e354428.json");
            GoogleCredentials credentials = GoogleCredentials.fromStream(resource.getInputStream());

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setDatabaseUrl("https://disasterhub-fbe9c-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

            System.out.println("Firebase initialized successfully");
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize Firebase: " + e.getMessage(), e);
        }
    }

    @Bean
    public FirebaseAuth firebaseAuth() {
        return FirebaseAuth.getInstance();
    }
}