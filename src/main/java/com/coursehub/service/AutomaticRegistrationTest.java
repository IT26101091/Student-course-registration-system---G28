package com.coursehub.service;

import com.coursehub.model.Course;
import com.coursehub.model.Registration;
import com.coursehub.repository.CourseRepository;
import com.coursehub.repository.RegistrationRepository;
import com.coursehub.repository.QueueRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class AutomaticRegistrationTest {

    public static void main(String[] args) throws Exception {
        Path courses = Path.of("data", "courses.json");
        Path registrations = Path.of("data", "registrations.json");
        Path queue = Path.of("data", "queue.json");

        Path backup = Files.createTempDirectory("coursehub-test");
        Path coursesBackup = backup.resolve("courses.json");
        Path registrationsBackup = backup.resolve("registrations.json");
        Path queueBackup = backup.resolve("queue.json");

        Files.copy(courses, coursesBackup);
        Files.copy(registrations, registrationsBackup);
        Files.copy(queue, queueBackup);

        try {
            Files.writeString(courses, "[]");
            Files.writeString(registrations, "[]");
            Files.writeString(queue, "[]");

            CourseRepository courseRepo = new CourseRepository();
            RegistrationRepository registrationRepo =
                    new RegistrationRepository();
            QueueRepository queueRepo = new QueueRepository();

            Course course = new Course(
                    "TEST101", "Test Course",
                    "Automatic registration test", 3, 1);

            if (!courseRepo.add(course)) {
                throw new AssertionError("Course setup failed.");
            }

            CourseRegistrationService service =
                    new CourseRegistrationService();

            check(service.registerStudent("TEST101", "STU-001"),
                    "Registration successful.");

            check(service.registerStudent("TEST101", "STU-002"),
                    "Course is full. Student added to waiting list.");

            if (queueRepo.count("TEST101") != 1) {
                throw new AssertionError("Waiting list setup failed.");
            }

            String result = service.cancelRegistration(
                    "TEST101", "STU-001");

            check(result,
                    "Registration cancelled. Waiting student STU-002 was registered.");

            List<Registration> active =
                    registrationRepo.findByCourse("TEST101");

            if (active.size() != 1
                    || !active.get(0).getStudentId().equals("STU-002")) {
                throw new AssertionError(
                        "Waiting student was not automatically registered.");
            }

            if (queueRepo.count("TEST101") != 0) {
                throw new AssertionError("Waiting list should be empty.");
            }

            System.out.println(
                    "PASS: Waiting student automatically registered.");
            System.out.println("PASS: FIFO waiting list emptied correctly.");

        } finally {
            Files.copy(coursesBackup, courses,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            Files.copy(registrationsBackup, registrations,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            Files.copy(queueBackup, queue,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void check(String actual, String expected) {
        if (!expected.equals(actual)) {
            throw new AssertionError(
                    "Expected: " + expected + ", got: " + actual);
        }
    }
}