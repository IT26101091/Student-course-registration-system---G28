
package com.coursehub;

import com.coursehub.repository.StudentRepository;
import com.coursehub.routes.StudentRoutes;
import com.coursehub.service.StudentService;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

import java.util.Map;

public class Main {

    public static void main(String[] args) {

        StudentRepository studentRepository =
                new StudentRepository();

        StudentService studentService =
                new StudentService(studentRepository);

        StudentRoutes studentRoutes =
                new StudentRoutes(studentService);

        Javalin app = Javalin.create(config -> {

            // Serve HTML, CSS and JavaScript.
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = "/public";
                staticFiles.location = Location.CLASSPATH;
            });

            // Server health check.
            config.routes.get("/api/health", ctx ->
                    ctx.json(Map.of(
                            "status", "running",
                            "application",
                            "Student Course Registration System"
                    ))
            );

            // Student CRUD API.
            config.routes.get(
                    "/api/students", studentRoutes::getAll);

            config.routes.get(
                    "/api/students/{studentId}",
                    studentRoutes::getOne);

            config.routes.post(
                    "/api/students", studentRoutes::create);

            config.routes.put(
                    "/api/students/{studentId}",
                    studentRoutes::update);

            config.routes.delete(
                    "/api/students/{studentId}",
                    studentRoutes::delete);

        }).start(7070);

        System.out.println(
                "Student Course Registration System is running.");
        System.out.println("Website: http://localhost:7070");
        System.out.println(
                "Students API: http://localhost:7070/api/students");
        System.out.println(
                "Health check: http://localhost:7070/api/health");
    }
}
