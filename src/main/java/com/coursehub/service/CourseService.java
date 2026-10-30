
package com.coursehub.service;

import com.coursehub.model.Course;
import com.coursehub.repository.CourseRepository;

import java.util.List;
import java.util.Optional;

public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService() {
        this.courseRepository = new CourseRepository();
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

        boolean deleted = courseRepository.deleteByCode(courseCode);

        if (!deleted) {
            throw new IllegalArgumentException(
                    "Course not found.");
        }
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
