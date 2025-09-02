package com.tursa.route;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.io.InputStream;

@SpringBootApplication
public class RouteApplication {

	public static void main(String[] args) {
		try {
			// Load the service account key from resources folder
			InputStream serviceAccount = RouteApplication.class
					.getClassLoader()
					.getResourceAsStream("disasterhub-fbe9c-firebase-adminsdk-fbsvc-ad7e354428.json");

			if (serviceAccount == null) {
				throw new RuntimeException("Firebase serviceAccountKey.json not found in resources folder!");
			}

			FirebaseOptions options = FirebaseOptions.builder()
					.setCredentials(GoogleCredentials.fromStream(serviceAccount))
					.setDatabaseUrl("https://disasterhub-fbe9c-default-rtdb.asia-southeast1.firebasedatabase.app/")
					.build();

			FirebaseApp.initializeApp(options);

			SpringApplication.run(RouteApplication.class, args);

		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to initialize Firebase", e);
		}
	}
}
