package com.enterprise.rag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@SpringBootApplication
public class VersionAwareRagApplication {

    public static void main(String[] args) {
        loadDotEnv();
        SpringApplication.run(VersionAwareRagApplication.class, args);
    }

    /**
     * Automatically loads .env variables into system properties if present,
     * allowing IDE Run/Debug actions to pick up Supabase & Groq configurations without manual shell exports.
     */
    private static void loadDotEnv() {
        String[] possiblePaths = {".env", "backend/.env", "../backend/.env", "../.env"};
        for (String path : possiblePaths) {
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                try {
                    List<String> lines = Files.readAllLines(file.toPath());
                    for (String line : lines) {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) {
                            continue;
                        }
                        int eqIdx = line.indexOf('=');
                        String key = line.substring(0, eqIdx).trim();
                        String val = line.substring(eqIdx + 1).trim();
                        // Only set if not already defined in system properties or environment
                        if (System.getProperty(key) == null && System.getenv(key) == null) {
                            System.setProperty(key, val);
                        }
                    }
                    System.out.println("[VersionAwareRagApplication] Loaded environment configuration from: " + file.getAbsolutePath());
                    break;
                } catch (IOException e) {
                    System.err.println("[VersionAwareRagApplication] Warning: Failed to parse " + path + ": " + e.getMessage());
                }
            }
        }
    }
}

