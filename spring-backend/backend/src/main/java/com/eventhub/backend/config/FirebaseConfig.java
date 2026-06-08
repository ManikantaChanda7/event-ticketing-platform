package com.eventhub.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        // First, check if a FirebaseApp already exists to avoid duplicates
        if (FirebaseApp.getApps().isEmpty()) {
            // For now, we'll use a dummy initialization (you'll need to download your serviceAccountKey.json)
            // In real setup, you'd load the service account file from resources or environment variable
            // For demo purposes, let's initialize with a default (or skip if you don't have the key yet)
            try {
                // Try to load service account key if present
                FileInputStream serviceAccount = new FileInputStream("src/main/resources/serviceAccountKey.json");
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
                return FirebaseApp.initializeApp(options);
            } catch (Exception e) {
                // If service account key is not present, still return a dummy (we'll handle token validation gracefully)
                System.err.println("Firebase service account key not found. OAuth login will use demo mode.");
                // Return a default (we'll add a check in the OAuth endpoint)
                return null;
            }
        } else {
            return FirebaseApp.getApps().get(0);
        }
    }
}
