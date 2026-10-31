
package com.coursehub.repository;

import com.coursehub.model.Student;
import com.fasterxml.jackson.core.type.TypeReference;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class StudentRepository {

    private final JsonFileStore<Student> store;

    public StudentRepository() {
        store = new JsonFileStore<>(
                Path.of("data", "students.json"),
                new TypeReference<List<Student>>() {}
        );
    }

    public synchronized List<Student> findAll() {
        return store.readAll();
    }

    public synchronized Optional<Student> findById(String studentId) {
        return store.readAll()
                .stream()
                .filter(student ->
                        student.getStudentId() != null
                                && student.getStudentId()
                                .equalsIgnoreCase(studentId))
                .findFirst();
    }

    public synchronized void insert(Student student) {
        List<Student> students = store.readAll();
        students.add(student);
        store.writeAll(students);
    }

    public synchronized boolean update(Student updatedStudent) {
        List<Student> students = store.readAll();

        for (int i = 0; i < students.size(); i++) {
            String currentId = students.get(i).getStudentId();

            if (currentId != null &&
                    currentId.equalsIgnoreCase(
                            updatedStudent.getStudentId())) {

                students.set(i, updatedStudent);
                store.writeAll(students);
                return true;
            }
        }

        return false;
    }

    public synchronized boolean deleteById(String studentId) {
        List<Student> students = store.readAll();

        boolean removed = students.removeIf(student ->
                student.getStudentId() != null
                        && student.getStudentId()
                        .equalsIgnoreCase(studentId));

        if (removed) {
            store.writeAll(students);
        }

        return removed;
    }
}

