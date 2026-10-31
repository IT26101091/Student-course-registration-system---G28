package com.coursehub.service;

import com.coursehub.model.Course;
import com.coursehub.model.Registration;
import com.coursehub.repository.CourseRepository;
import com.coursehub.repository.RegistrationRepository;

public class CourseRegistrationService {

    private final CourseRepository courseRepository =
            new CourseRepository();

    private final RegistrationRepository registrationRepository =
            new RegistrationRepository();

    private final CourseWaitingListService waitingListService =
            new CourseWaitingListService();

    public String registerStudent(String courseCode, String studentId) {
        if (courseCode == null || courseCode.isBlank()
                || studentId == null || studentId.isBlank()) {
            return "Invalid course code or student ID.";
        }

        Course course = courseRepository.findByCode(courseCode);

        if (course == null) {
            return "Course not found.";
        }

        boolean alreadyRegistered = registrationRepository.findAll()
                .stream()
                .anyMatch(r ->
                        studentId.equals(r.getStudentId())
                                && course.getCourseCode()
                                .equalsIgnoreCase(r.getCourseCode())
                                && Registration.ACTIVE.equals(r.getStatus())
                );

        if (alreadyRegistered) {
            return "Student is already registered for this course.";
        }

        if (registrationRepository.countActive(course.getCourseCode())
                < course.getCapacity()) {

            boolean added = registrationRepository.add(
                    studentId,
                    course.getCourseCode()
            );

            return added
                    ? "Registration successful."
                    : "Registration failed.";
        }

        boolean queued = waitingListService.joinWaitingList(
                course.getCourseCode(),
                studentId
        );

        if (queued) {
            return "Course is full. Student added to waiting list.";
        }

        return "Could not join waiting list. Student may already be waiting.";
    }

    public String cancelRegistration(String courseCode, String studentId) {
        if (courseCode == null || courseCode.isBlank()
                || studentId == null || studentId.isBlank()) {
            return "Invalid course code or student ID.";
        }

        Course course = courseRepository.findByCode(courseCode);

        if (course == null) {
            return "Course not found.";
        }

        boolean cancelled = registrationRepository.cancel(
                studentId,
                course.getCourseCode()
        );

        if (!cancelled) {
            return "Active registration not found.";
        }

        String nextStudent = waitingListService.peekNextStudent(
                course.getCourseCode()
        );

        if (nextStudent == null) {
            return "Registration cancelled. No students are waiting.";
        }

        boolean registered = registrationRepository.add(
                nextStudent,
                course.getCourseCode()
        );

        if (!registered) {
            return "Registration cancelled. The next waiting student remains in the queue because registration failed.";
        }

        String removedStudent = waitingListService.removeNextStudent(
                course.getCourseCode()
        );

        if (removedStudent == null || !nextStudent.equals(removedStudent)) {
            return "Registration cancelled. Please check the waiting queue and registrations.";
        }

        return "Registration cancelled. Waiting student "
                + nextStudent + " was registered.";
    }
}