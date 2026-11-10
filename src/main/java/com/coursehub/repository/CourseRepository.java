
package com.coursehub.repository;

import com.coursehub.model.Course;
import com.fasterxml.jackson.core.type.TypeReference;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class CourseRepository {

    private final JsonFileStore<Course> store;

    public CourseRepository() {
        store = new JsonFileStore<>(
                Path.of("data", "courses.json"),
                new TypeReference<List<Course>>() {}
        );
    }

    // Get all courses
    public synchronized List<Course> findAll() {
        return store.readAll();
    }

    // Find a course using its course code
    public synchronized Optional<Course> findByCode(String courseCode) {
        if (courseCode == null || courseCode.isBlank()) {
            return Optional.empty();
        }

        return store.readAll()
                .stream()
                .filter(course ->
                        course.getCourseCode() != null
                                && course.getCourseCode()
                                .equalsIgnoreCase(courseCode.trim()))
                .findFirst();
    }

    // Add a new course
    public synchronized void insert(Course course) {
        if (course == null) {
            throw new IllegalArgumentException(
                    "Course cannot be null.");
        }

        if (findByCode(course.getCourseCode()).isPresent()) {
            throw new IllegalArgumentException(
                    "A course with this code already exists.");
        }

        List<Course> courses = store.readAll();
        courses.add(course);
        store.writeAll(courses);
    }

    // Update an existing course
    public synchronized boolean update(Course updatedCourse) {
        if (updatedCourse == null
                || updatedCourse.getCourseCode() == null) {
            return false;
        }

        List<Course> courses = store.readAll();

        for (int i = 0; i < courses.size(); i++) {
            String currentCode =
                    courses.get(i).getCourseCode();

            if (currentCode != null
                    && currentCode.equalsIgnoreCase(
                    updatedCourse.getCourseCode().trim())) {

                courses.set(i, updatedCourse);
                store.writeAll(courses);
                return true;
            }
        }

        return false;
    }

    // Delete a course using its course code
    public synchronized boolean deleteByCode(String courseCode) {
        if (courseCode == null || courseCode.isBlank()) {
            return false;
        }

        List<Course> courses = store.readAll();

        boolean removed = courses.removeIf(course ->
                course.getCourseCode() != null
                        && course.getCourseCode()
                        .equalsIgnoreCase(courseCode.trim()));

        if (removed) {
            store.writeAll(courses);
        }

        return removed;
    }
}

