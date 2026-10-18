
package com.coursehub.service;

import com.coursehub.model.Student;
import com.coursehub.repository.StudentRepository;

import java.util.List;
import java.util.Objects;

public class StudentService {

    private final StudentRepository repository;

    public StudentService(StudentRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public synchronized List<Student> getAllStudents() {
        return repository.findAll();
    }

    public synchronized Student getStudent(String studentId) {
        String id = require(studentId, "Student ID");

        return repository.findById(id)
                .orElseThrow(() ->
                        new StudentNotFoundException(
                                "Student not found: " + id));
    }

    public synchronized Student createStudent(Student student) {
        if (student == null) {
            throw new IllegalArgumentException(
                    "Student details are required.");
        }

        student.setStudentId(
                require(student.getStudentId(), "Student ID"));

        validateEditableFields(student);

        if (repository.findById(student.getStudentId()).isPresent()) {
            throw new DuplicateStudentException(
                    "Student ID already exists: "
                            + student.getStudentId());
        }

        repository.insert(student);
        return student;
    }

    public synchronized Student updateStudent(
            String studentId, Student updatedStudent) {

        String id = require(studentId, "Student ID");

        Student existing = getStudent(id);

        if (updatedStudent == null) {
            throw new IllegalArgumentException(
                    "Updated student details are required.");
        }

        validateEditableFields(updatedStudent);

        // Keep the original ID; it cannot be changed by editing.
        existing.setName(updatedStudent.getName());
        existing.setEmail(updatedStudent.getEmail());
        existing.setDegreeProgram(
                updatedStudent.getDegreeProgram());

        if (!repository.update(existing)) {
            throw new StudentNotFoundException(
                    "Student not found: " + id);
        }

        return existing;
    }

    public synchronized void deleteStudent(String studentId) {
        String id = require(studentId, "Student ID");

        if (!repository.deleteById(id)) {
            throw new StudentNotFoundException(
                    "Student not found: " + id);
        }
    }

    private void validateEditableFields(Student student) {
        student.setName(require(student.getName(), "Name"));
        student.setEmail(require(student.getEmail(), "Email"));
        student.setDegreeProgram(
                require(student.getDegreeProgram(), "Degree program"));

        if (!student.getEmail().matches(
                "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException(
                    "Enter a valid email address.");
        }
    }

    private String require(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " is required.");
        }

        return value.trim();
    }

    public static class StudentNotFoundException
            extends RuntimeException {
        public StudentNotFoundException(String message) {
            super(message);
        }
    }

    public static class DuplicateStudentException
            extends RuntimeException {
        public DuplicateStudentException(String message) {
            super(message);
        }
    }
}
