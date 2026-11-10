
package com.coursehub.service;

import com.coursehub.model.Course;
import com.coursehub.repository.CourseRepository;
import com.coursehub.model.Registration;
import com.coursehub.repository.RegistrationRepository;
import com.coursehub.repository.QueueRepository;

import java.util.List;
import java.util.Optional;

public class CourseService {

    private final CourseRepository courseRepository;
    private final RegistrationRepository registrationRepository;
    private final QueueRepository queueRepository;

    public CourseService() {
        this(
                new CourseRepository(),
                new RegistrationRepository(),
                new QueueRepository()
        );
    }

    public CourseService(
            CourseRepository courseRepository,
            RegistrationRepository registrationRepository,
            QueueRepository queueRepository) {

        this.courseRepository = courseRepository;
        this.registrationRepository = registrationRepository;
        this.queueRepository = queueRepository;
    }

    // Get all courses
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    // Get a course by course code
    public Optional<Course> getCourseByCode(String courseCode) {
        return courseRepository.findByCode(courseCode);
    }

    // Add a new course
    public void addCourse(Course course) {
        validateCourse(course);

        if (courseRepository.findByCode(course.getCourseCode()).isPresent()) {
            throw new IllegalArgumentException(
                    "Course code already exists.");
        }

        courseRepository.insert(course);
    }

    // Update an existing course
    public void updateCourse(String courseCode, Course course) {
        validateCourse(course);

        if (courseCode == null || courseCode.isBlank()) {
            throw new IllegalArgumentException(
                    "Course code is required.");
        }

        if (!courseCode.equalsIgnoreCase(course.getCourseCode().trim())) {
            throw new IllegalArgumentException(
                    "Course code cannot be changed.");
        }

        boolean updated = courseRepository.update(course);

        if (!updated) {
            throw new IllegalArgumentException(
                    "Course not found.");
        }
    }

    // Delete a course
    public void deleteCourse(String courseCode) {

        if (courseCode == null || courseCode.isBlank()) {
            throw new IllegalArgumentException(
                    "Course code is required.");
        }

        // Check whether the course exists.
        if (courseRepository.findByCode(courseCode).isEmpty()) {
            throw new IllegalArgumentException(
                    "Course not found.");
        }

        // Check for active registrations.
        long activeRegistrations =
                registrationRepository.findByCourseCode(courseCode)
                        .stream()
                        .filter(registration ->
                                Registration.ACTIVE.equalsIgnoreCase(
                                        registration.getStatus()))
                        .count();

        // Check for waiting students.
        long waitingStudents =
                queueRepository.findWaitingByCourseCode(courseCode).size();

        // Prevent deletion if either exists.
        if (activeRegistrations > 0 || waitingStudents > 0) {

            throw new IllegalStateException(
                    "Cannot delete this course. It has "
                            + activeRegistrations
                            + " active registrations and "
                            + waitingStudents
                            + " students in the waiting list."
            );
        }

        // Delete only if no active registrations or waiting students exist.
        courseRepository.deleteByCode(courseCode);
    }

    // Validate course details
    private void validateCourse(Course course) {
        if (course == null) {
            throw new IllegalArgumentException(
                    "Course details are required.");
        }

        if (course.getCourseCode() == null
                || course.getCourseCode().isBlank()) {
            throw new IllegalArgumentException(
                    "Course code is required.");
        }

        if (course.getTitle() == null
                || course.getTitle().isBlank()) {
            throw new IllegalArgumentException(
                    "Course title is required.");
        }

        if (course.getCredits() <= 0) {
            throw new IllegalArgumentException(
                    "Credits must be greater than zero.");
        }

        if (course.getCapacity() <= 0) {
            throw new IllegalArgumentException(
                    "Course capacity must be greater than zero.");
        }
    }
}
