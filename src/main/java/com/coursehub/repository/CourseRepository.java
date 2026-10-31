
package com.coursehub.repository;

import com.coursehub.model.Course;
import com.fasterxml.jackson.core.type.TypeReference;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CourseRepository {

    private final JsonFileStore<Course> store;

    public CourseRepository() {
        store = new JsonFileStore<>(
                Path.of("data", "courses.json"),
                new TypeReference<List<Course>>() {}
        );
    }

    public synchronized List<Course> findAll() {
        return store.readAll();
    }

    public synchronized Course findByCode(String courseCode) {
        if (courseCode == null || courseCode.isBlank()) {
            return null;
        }

        for (Course course : store.readAll()) {
            if (courseCode.equalsIgnoreCase(course.getCourseCode())) {
                return course;
            }
        }

        return null;
    }

    public synchronized boolean add(Course course) {
        if (course == null
                || course.getCourseCode() == null
                || course.getCourseCode().isBlank()
                || course.getTitle() == null
                || course.getTitle().isBlank()
                || course.getCapacity() < 0) {
            return false;
        }

        List<Course> courses = store.readAll();

        for (Course existing : courses) {
            if (course.getCourseCode()
                    .equalsIgnoreCase(existing.getCourseCode())) {
                return false;
            }
        }

        courses.add(course);
        store.writeAll(courses);
        return true;
    }
}
