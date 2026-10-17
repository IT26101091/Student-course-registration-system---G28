package com.coursehub;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            // Serve HTML, CSS and JavaScript from src/main/resources/public.
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = "/public";
                staticFiles.location = Location.CLASSPATH;
            });

            // In Javalin 7, define routes during application configuration.
            config.routes.get("/api/health", ctx -> ctx.json(Map.of(
                    "status", "running",
                    "application", "Student Course Registration System"
            )));
        }).start(7070);

        System.out.println("Student Course Registration System is running.");
        System.out.println("Open http://localhost:7070 in your browser.");
        System.out.println("Health check: http://localhost:7070/api/health");
    }
}
