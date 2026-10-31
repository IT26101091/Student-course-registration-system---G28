
package com.coursehub.repository;

import com.coursehub.model.Registration;
import com.fasterxml.jackson.core.type.TypeReference;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RegistrationRepository {

    private final JsonFileStore<Registration> store;

    public RegistrationRepository() {
        store = new JsonFileStore<>(
                Path.of("data", "registrations.json"),
                new TypeReference<List<Registration>>() {}
        );
    }

    public synchronized List<Registration> findAll() {
        return store.readAll();
    }

    public synchronized List<Registration> findByCourse(
            String courseCode) {

        List<Registration> result = new ArrayList<>();

        if (courseCode == null || courseCode.isBlank()) {
            return result;
        }

        for (Registration registration : store.readAll()) {
            if (courseCode.equals(registration.getCourseCode())
                    && Registration.ACTIVE.equals(
                    registration.getStatus())) {
                result.add(registration);
            }
        }

        return result;
    }

    public synchronized boolean add(
            String studentId, String courseCode) {

        if (studentId == null || studentId.isBlank()
                || courseCode == null || courseCode.isBlank()) {
            return false;
        }

        List<Registration> registrations = store.readAll();

        for (Registration registration : registrations) {
            if (studentId.equals(registration.getStudentId())
                    && courseCode.equals(registration.getCourseCode())
                    && Registration.ACTIVE.equals(
                    registration.getStatus())) {
                return false;
            }
        }

        Registration registration = new Registration(
                UUID.randomUUID().toString(),
                studentId,
                courseCode,
                java.time.LocalDateTime.now().toString(),
                Registration.ACTIVE
        );

        registrations.add(registration);
        store.writeAll(registrations);

        return true;
    }

    public synchronized boolean cancel(
            String studentId, String courseCode) {

        List<Registration> registrations = store.readAll();

        for (Registration registration : registrations) {
            if (studentId != null
                    && courseCode != null
                    && studentId.equals(registration.getStudentId())
                    && courseCode.equals(registration.getCourseCode())
                    && Registration.ACTIVE.equals(
                    registration.getStatus())) {

                registration.setStatus(Registration.CANCELLED);
                store.writeAll(registrations);
                return true;
            }
        }

        return false;
    }

    public synchronized int countActive(String courseCode) {
        return findByCourse(courseCode).size();
    }
}
