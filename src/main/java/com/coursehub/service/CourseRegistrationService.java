
package com.coursehub.service;

import com.coursehub.model.Course;
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

        if (registrationRepository.findAll().stream()
                .anyMatch(r -> studentId.equals(r.getStudentId())
                        && courseCode.equalsIgnoreCase(r.getCourseCode())
                        && com.coursehub.model.Registration.ACTIVE
                        .equals(r.getStatus()))) {
            return "Student is already registered for this course.";
        }

        if (registrationRepository.countActive(courseCode)
                < course.getCapacity()) {
            boolean added = registrationRepository.add(
                    studentId, course.getCourseCode());

            return added
                    ? "Registration successful."
                    : "Registration failed.";
        }

        boolean queued = waitingListService.joinWaitingList(
                course.getCourseCode(), studentId);

        return queued
                ? "Course is full. Student added to waiting list."
                : "Could not join waiting list. Student may already be waiting.";
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
                studentId, course.getCourseCode());

        if (!cancelled) {
            return "Active registration not found.";
        }

        String nextStudent = waitingListService.getNextStudent(
                course.getCourseCode());

        if (nextStudent != null) {
            boolean registered = registrationRepository.add(
                    nextStudent, course.getCourseCode());

            if (!registered) {

                waitingListService.joinWaitingList(
                        course.getCourseCode(), nextStudent);

                return "Registration cancelled, but the next waiting student could not be registered.";
            }

            return "Registration cancelled. Waiting student "
                    + nextStudent + " was registered.";
        }

        return "Registration cancelled. No students are waiting.";
    }
}
