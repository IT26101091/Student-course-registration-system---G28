
package com.coursehub;

import com.coursehub.repository.StudentRepository;
import com.coursehub.routes.StudentRoutes;
import com.coursehub.service.StudentService;
import com.coursehub.repository.CourseRepository;
import com.coursehub.service.CourseService;
import com.coursehub.routes.CourseRoutes;
import com.coursehub.repository.AccountRepository;
import com.coursehub.routes.AuthRoutes;
import com.coursehub.service.AccountService;
import com.coursehub.routes.AuthAccessControl;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.UnauthorizedResponse;
import com.coursehub.routes.AdminAccountRoutes;

import java.util.Map;

public class Main {

    public static void main(String[] args) {

        StudentRepository studentRepository =
                new StudentRepository();

        StudentService studentService =
                new StudentService(studentRepository);

        StudentRoutes studentRoutes =
                new StudentRoutes(studentService);

        // Course management
        CourseRepository courseRepository =
                new CourseRepository();

        CourseService courseService =
                new CourseService();

        CourseRoutes courseRoutes =
                new CourseRoutes(courseService);


        AccountRepository accountRepository =
                new AccountRepository();

        AccountService accountService =
                new AccountService(accountRepository);

        AuthRoutes authRoutes =
                new AuthRoutes(accountService, accountRepository);

        AdminAccountRoutes adminAccountRoutes =
                new AdminAccountRoutes(
                        accountService,
                        studentRepository
                );

        AuthAccessControl accessControl =
                new AuthAccessControl(accountRepository);

        boolean adminExists = accountRepository.findAll()
                .stream()
                .anyMatch(account ->
                        "ADMIN".equalsIgnoreCase(account.getRole()));

        if (!adminExists) {

            String adminUsername =
                    System.getenv("COURSEHUB_ADMIN_USERNAME");

            String adminPassword =
                    System.getenv("COURSEHUB_ADMIN_PASSWORD");

            if (adminUsername == null || adminUsername.isBlank()
                    || adminPassword == null || adminPassword.isBlank()) {
                throw new IllegalStateException(
                        "Set COURSEHUB_ADMIN_USERNAME and "
                                + "COURSEHUB_ADMIN_PASSWORD in IntelliJ "
                                + "before starting the server.");
            }

            accountService.createAdminAccount(
                    adminUsername,
                    adminPassword);

            System.out.println("Initial Admin account created.");
        }

        Javalin app = Javalin.create(config -> {

            // Serve HTML, CSS and JavaScript.
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = "/public";
                staticFiles.location = Location.CLASSPATH;
            });

            // Enforce login and access rules for API requests.
            config.routes.before(
                    "/api/*",
                    accessControl::checkAccess
            );

            // Return a JSON response for unauthorized requests.
            config.routes.exception(
                    UnauthorizedResponse.class,
                    (exception, ctx) -> {
                        ctx.status(401).json(
                                java.util.Map.of(
                                        "error",
                                        exception.getMessage() == null
                                                ? "Please log in."
                                                : exception.getMessage()
                                )
                        );
                    }
            );

// Return a JSON response for forbidden requests.
            config.routes.exception(
                    ForbiddenResponse.class,
                    (exception, ctx) -> {
                        ctx.status(403).json(
                                java.util.Map.of(
                                        "error",
                                        exception.getMessage() == null
                                                ? "Access denied."
                                                : exception.getMessage()
                                )
                        );
                    }
            );

            // Server health check.
            config.routes.get("/api/health", ctx ->
                    ctx.json(Map.of(
                            "status", "running",
                            "application",
                            "Student Course Registration System"
                    ))
            );

            // Authentication routes
            config.routes.post(
                    "/api/auth/login",
                    authRoutes::login
            );

            config.routes.post(
                    "/api/auth/logout",
                    authRoutes::logout
            );

            config.routes.get(
                    "/api/auth/me",
                    authRoutes::currentAccount
            );

            config.routes.post(
                    "/api/auth/change-password",
                    authRoutes::changePassword
            );

            config.routes.post(
                    "/api/auth/change-username",
                    authRoutes::changeUsername
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

            // Course CRUD API.
            config.routes.get(
                    "/api/courses",
                    courseRoutes::getAll);

            config.routes.get(
                    "/api/courses/{courseCode}",
                    courseRoutes::getOne);

            config.routes.post(
                    "/api/courses",
                    courseRoutes::create);

            config.routes.put(
                    "/api/courses/{courseCode}",
                    courseRoutes::update);

            config.routes.delete(
                    "/api/courses/{courseCode}",
                    courseRoutes::delete);

            //student account creation
            config.routes.post(
                    "/api/admin/accounts",
                    adminAccountRoutes::createStudentAccount
            );


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
