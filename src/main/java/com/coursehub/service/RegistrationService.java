package com.coursehub.service;

import com.coursehub.model.Course;
import com.coursehub.model.Registration;
import com.coursehub.repository.RegistrationRepository;
import com.coursehub.repository.StudentRepository;
import com.coursehub.repository.CourseRepository;
import com.coursehub.sorting.RegistrationInsertionSort;

import java.time.LocalDateTime;import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    // Connect the repositories.
    public RegistrationService() {
        this(
                new RegistrationRepository(),
                new StudentRepository(),
                new CourseRepository()
        );
    }

    public RegistrationService(
            RegistrationRepository registrationRepository,
            StudentRepository studentRepository,
            CourseRepository courseRepository) {

        this.registrationRepository = registrationRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    // READ: Get all registrations.
    public List<Registration> getAllRegistrations() {
        return registrationRepository.findAll();
    }

    // READ: Get all registrations sorted by registration time.
    public List<Registration> getRegistrationsSortedByTime() {
        return RegistrationInsertionSort.sortByRegistrationTime(
                registrationRepository.findAll()
        );
    }

    // READ: Get one student's registrations sorted by registration time.
    public List<Registration> getStudentRegistrationsSortedByTime(
            String studentId) {

        return RegistrationInsertionSort.sortByRegistrationTime(
                getRegistrationsByStudent(studentId)
        );
    }

    // READ: Get a registration using its ID.
    public Optional<Registration> getRegistrationById(
            String registrationId) {

        return registrationRepository.findById(registrationId);
    }

    // READ: Get registrations belonging to a student.
    public List<Registration> getRegistrationsByStudent(
            String studentId) {

        require(studentId, "Student ID");

        if (studentRepository.findById(studentId).isEmpty()) {
            throw new IllegalArgumentException(
                    "Student does not exist.");
        }

        return registrationRepository.findByStudentId(studentId);
    }

    // READ: Get registrations belonging to a course.
    public List<Registration> getRegistrationsByCourse(
            String courseCode) {

        require(courseCode, "Course code");

        if (courseRepository.findByCode(courseCode).isEmpty()) {
            throw new IllegalArgumentException(
                    "Course does not exist.");
        }

        return registrationRepository.findByCourseCode(courseCode);
    }

    // CREATE: Register a student for a course.
    public synchronized Registration registerStudentForCourse(
            String studentId,
            String courseCode) {

        require(studentId, "Student ID");
        require(courseCode, "Course code");

        studentId = studentId.trim();
        courseCode = courseCode.trim();

        // Check whether the student exists.
        if (studentRepository.findById(studentId).isEmpty()) {
            throw new IllegalArgumentException(
                    "Student does not exist.");
        }

        // Check whether the course exists.
        Course course = courseRepository.findByCode(courseCode)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Course does not exist."));

        // Prevent duplicate active registrations.
        if (registrationRepository.findActiveRegistration(
                studentId, courseCode).isPresent()) {

            throw new IllegalArgumentException(
                    "Student is already registered for this course.");
        }

        // Count active registrations for this course.
        long activeCount =
                registrationRepository.findByCourseCode(courseCode)
                        .stream()
                        .filter(registration ->
                                Registration.ACTIVE.equalsIgnoreCase(
                                        registration.getStatus()))
                        .count();

        // Check course capacity.
        if (activeCount >= course.getCapacity()) {
            throw new IllegalStateException(
                    "Course is full. Waiting-list integration "
                            + "will be added later.");
        }

        // Create the new registration.
        Registration registration = new Registration(
                "REG-" + UUID.randomUUID(),
                studentId,
                courseCode,
                LocalDateTime.now(),
                Registration.ACTIVE
        );

        registrationRepository.insert(registration);

        return registration;
    }

    // UPDATE: Drop a course by cancelling the registration.
    public synchronized Registration dropCourse(
            String studentId,
            String courseCode) {

        require(studentId, "Student ID");
        require(courseCode, "Course code");

        Registration registration =
                registrationRepository.findActiveRegistration(
                                studentId.trim(),
                                courseCode.trim())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No active registration found."));

        registration.setStatus(Registration.CANCELLED);

        registrationRepository.update(registration);

        return registration;
    }

    // UPDATE: Change registration status.
    public synchronized Registration changeRegistrationStatus(
            String registrationId,
            String newStatus) {

        require(registrationId, "Registration ID");
        require(newStatus, "Registration status");

        String status = newStatus.trim().toUpperCase();

        if (!status.equals(Registration.ACTIVE)
                && !status.equals(Registration.CANCELLED)) {

            throw new IllegalArgumentException(
                    "Invalid registration status.");
        }

        Registration registration =
                registrationRepository.findById(registrationId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Registration not found."));

        if (Registration.ACTIVE.equals(status)
                && !Registration.ACTIVE.equals(
                registration.getStatus())) {

            Course course = courseRepository.findByCode(
                            registration.getCourseCode())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Course does not exist."));

            if (studentRepository.findById(
                    registration.getStudentId()).isEmpty()) {
                throw new IllegalArgumentException(
                        "Student does not exist.");
            }

            if (registrationRepository.findActiveRegistration(
                    registration.getStudentId(),
                    registration.getCourseCode()).isPresent()) {

                throw new IllegalArgumentException(
                        "Student already has an active registration.");
            }

            long activeCount =
                    registrationRepository.findByCourseCode(
                                    registration.getCourseCode())
                            .stream()
                            .filter(existing ->
                                    Registration.ACTIVE.equalsIgnoreCase(
                                            existing.getStatus()))
                            .count();

            if (activeCount >= course.getCapacity()) {
                throw new IllegalStateException(
                        "Course is full.");
            }
        }

        registration.setStatus(status);

        registrationRepository.update(registration);

        return registration;
    }

    // DELETE: Permanently remove a registration.
    // Reserve this operation for authorised administrative use.
    public synchronized boolean deleteRegistration(
            String registrationId) {

        require(registrationId, "Registration ID");

        return registrationRepository.deleteById(registrationId);
    }

    // Common input validation.
    private void require(String value, String field) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " is required.");
        }
    }
}