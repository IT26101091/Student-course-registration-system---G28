
package com.coursehub.repository;

import com.coursehub.model.Registration;
import com.fasterxml.jackson.core.type.TypeReference;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

    public class RegistrationRepository {

        private final JsonFileStore<Registration> store;

        // Connect to the registrations JSON file.
        public RegistrationRepository() {
            this(Path.of("data", "registrations.json"));
        }

        // Constructor for testing with a separate JSON file.
        public RegistrationRepository(Path filePath) {
            store = new JsonFileStore<>(
                    filePath,
                    new TypeReference<List<Registration>>() {}
            );
        }

        // Retrieve all registrations.
        public synchronized List<Registration> findAll() {
            return store.readAll();
        }

        // Find registration using its ID.
        public synchronized Optional<Registration> findById(
                String registrationId) {

            if (registrationId == null || registrationId.isBlank()) {
                return Optional.empty();
            }

            return store.readAll().stream()
                    .filter(registration ->
                            registration.getRegistrationId() != null
                                    && registration.getRegistrationId()
                                    .equalsIgnoreCase(registrationId.trim()))
                    .findFirst();
        }

        // Find all registrations belonging to a student.
        public synchronized List<Registration> findByStudentId(
                String studentId) {

            if (studentId == null || studentId.isBlank()) {
                return List.of();
            }

            return store.readAll().stream()
                    .filter(registration ->
                            registration.getStudentId() != null
                                    && registration.getStudentId()
                                    .equalsIgnoreCase(studentId.trim()))
                    .toList();
        }

        // Find all registrations for a particular course.
        public synchronized List<Registration> findByCourseCode(
                String courseCode) {

            if (courseCode == null || courseCode.isBlank()) {
                return List.of();
            }

            return store.readAll().stream()
                    .filter(registration ->
                            registration.getCourseCode() != null
                                    && registration.getCourseCode()
                                    .equalsIgnoreCase(courseCode.trim()))
                    .toList();
        }

        // Find an active registration for a student and course.
        public synchronized Optional<Registration> findActiveRegistration(
                String studentId,
                String courseCode) {

            return findAll().stream()
                    .filter(registration ->
                            Registration.ACTIVE.equalsIgnoreCase(
                                    registration.getStatus()))
                    .filter(registration ->
                            registration.getStudentId() != null
                                    && registration.getStudentId()
                                    .equalsIgnoreCase(studentId))
                    .filter(registration ->
                            registration.getCourseCode() != null
                                    && registration.getCourseCode()
                                    .equalsIgnoreCase(courseCode))
                    .findFirst();
        }

        // Insert a new registration.
        public synchronized void insert(Registration registration) {

            validateRegistration(registration);

            List<Registration> registrations = store.readAll();

            boolean duplicateId = registrations.stream()
                    .anyMatch(existing ->
                            existing.getRegistrationId() != null
                                    && existing.getRegistrationId()
                                    .equalsIgnoreCase(
                                            registration.getRegistrationId().trim()));

            if (duplicateId) {
                throw new IllegalArgumentException(
                        "Registration ID already exists.");
            }

            registrations.add(registration);
            store.writeAll(registrations);
        }

        // Update an existing registration.
        public synchronized boolean update(
                Registration updatedRegistration) {

            validateRegistration(updatedRegistration);

            List<Registration> registrations = store.readAll();

            for (int i = 0; i < registrations.size(); i++) {

                String currentId =
                        registrations.get(i).getRegistrationId();

                if (currentId != null
                        && currentId.equalsIgnoreCase(
                        updatedRegistration.getRegistrationId().trim())) {

                    registrations.set(i, updatedRegistration);
                    store.writeAll(registrations);

                    return true;
                }
            }

            return false;
        }

        // Permanently delete a registration.
        public synchronized boolean deleteById(String registrationId) {

            if (registrationId == null || registrationId.isBlank()) {
                return false;
            }

            List<Registration> registrations = store.readAll();

            boolean removed = registrations.removeIf(registration ->
                    registration.getRegistrationId() != null
                            && registration.getRegistrationId()
                            .equalsIgnoreCase(registrationId.trim()));

            if (removed) {
                store.writeAll(registrations);
            }

            return removed;
        }

        // Validate registration information.
        private void validateRegistration(Registration registration) {

            if (registration == null) {
                throw new IllegalArgumentException(
                        "Registration is required.");
            }

            require(registration.getRegistrationId(), "Registration ID");
            require(registration.getStudentId(), "Student ID");
            require(registration.getCourseCode(), "Course code");
            require(registration.getRegistrationDateTime(), "Registration date and time");

            String status = registration.getStatus();

            if (status == null
                    || (!Registration.ACTIVE.equalsIgnoreCase(status)
                    && !Registration.CANCELLED.equalsIgnoreCase(status))) {

                throw new IllegalArgumentException(
                        "Invalid registration status.");
            }
        }

        private void require(String value, String field) {

            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException(
                        field + " is required.");
            }
        }

        private void require(LocalDateTime value, String field) {

            if (value == null) {
                throw new IllegalArgumentException(
                        field + " is required.");
            }
        }
    }

