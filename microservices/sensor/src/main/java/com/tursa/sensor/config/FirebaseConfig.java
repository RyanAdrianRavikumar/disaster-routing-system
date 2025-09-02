package com.tursa.sensor.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initializeFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                // Load the service account key from resources folder
                InputStream serviceAccount = getClass().getClassLoader()
                        .getResourceAsStream("disasterhub-fbe9c-firebase-adminsdk-fbsvc-ad7e354428.json");

                if (serviceAccount == null) {
                    throw new IOException("Firebase serviceAccountKey.json not found in resources folder!");
                }

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setDatabaseUrl("https://disasterhub-fbe9c-default-rtdb.asia-southeast1.firebasedatabase.app/")
                        .build();

                FirebaseApp.initializeApp(options);
                System.out.println("Firebase initialized successfully in sensor module");
            }
        } catch (IOException e) {
            System.err.println("Firebase initialization failed: " + e.getMessage());
        }
    }
}