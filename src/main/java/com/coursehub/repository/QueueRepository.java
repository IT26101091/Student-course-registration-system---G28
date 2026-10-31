package com.coursehub.repository;

import com.coursehub.model.QueueEntry;
import com.fasterxml.jackson.core.type.TypeReference;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QueueRepository {

    private final JsonFileStore<QueueEntry> store;

    public QueueRepository() {
        store = new JsonFileStore<>(
                Path.of("data", "queue.json"),
                new TypeReference<List<QueueEntry>>() {}
        );
    }

    public synchronized List<QueueEntry> findAll() {
        return store.readAll();
    }

    public synchronized List<QueueEntry> findByCourse(
            String courseCode) {

        List<QueueEntry> result = new ArrayList<>();

        if (courseCode == null || courseCode.isBlank()) {
            return result;
        }

        for (QueueEntry entry : store.readAll()) {
            if (courseCode.equals(entry.getCourseCode())) {
                result.add(entry);
            }
        }

        return result;
    }

    public synchronized boolean add(
            String courseCode, String studentId) {

        if (courseCode == null || courseCode.isBlank()
                || studentId == null || studentId.isBlank()) {
            return false;
        }

        List<QueueEntry> entries = store.readAll();

        for (QueueEntry entry : entries) {
            if (courseCode.equals(entry.getCourseCode())
                    && studentId.equals(entry.getStudentId())) {
                return false;
            }
        }

        QueueEntry newEntry = new QueueEntry(
                UUID.randomUUID().toString(),
                studentId,
                courseCode,
                java.time.LocalDateTime.now().toString()
        );

        entries.add(newEntry);
        store.writeAll(entries);

        return true;
    }


    public synchronized QueueEntry peekFirst(String courseCode) {

        if (courseCode == null || courseCode.isBlank()) {
            return null;
        }

        for (QueueEntry entry : store.readAll()) {
            if (courseCode.equals(entry.getCourseCode())) {
                return entry;
            }
        }

        return null;
    }


    public synchronized QueueEntry removeFirst(String courseCode) {

        if (courseCode == null || courseCode.isBlank()) {
            return null;
        }

        List<QueueEntry> entries = store.readAll();

        for (int i = 0; i < entries.size(); i++) {
            QueueEntry entry = entries.get(i);

            if (courseCode.equals(entry.getCourseCode())) {
                entries.remove(i);
                store.writeAll(entries);
                return entry;
            }
        }

        return null;
    }

    public synchronized int count(String courseCode) {
        return findByCourse(courseCode).size();
    }
}