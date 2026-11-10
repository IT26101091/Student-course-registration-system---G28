
package com.coursehub.repository;

import com.coursehub.model.QueueEntry;
import com.fasterxml.jackson.core.type.TypeReference;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class QueueRepository {

    private final JsonFileStore<QueueEntry> store;

    // Connect to the queue JSON file.
    public QueueRepository() {
        this(Path.of("data", "queue.json"));
    }

    // Constructor for testing with a separate JSON file.
    public QueueRepository(Path filePath) {
        store = new JsonFileStore<>(
                filePath,
                new TypeReference<List<QueueEntry>>() {}
        );
    }

    // Retrieve all queue entries.
    public synchronized List<QueueEntry> findAll() {
        return store.readAll();
    }

    // Find a queue entry using its ID.
    public synchronized Optional<QueueEntry> findById(
            String queueId) {

        if (queueId == null || queueId.isBlank()) {
            return Optional.empty();
        }

        return store.readAll().stream()
                .filter(entry ->
                        entry.getQueueId() != null
                                && entry.getQueueId()
                                .equalsIgnoreCase(queueId.trim()))
                .findFirst();
    }

    // Find all queue entries belonging to a student.
    public synchronized List<QueueEntry> findByStudentId(
            String studentId) {

        if (studentId == null || studentId.isBlank()) {
            return List.of();
        }

        return store.readAll().stream()
                .filter(entry ->
                        entry.getStudentId() != null
                                && entry.getStudentId()
                                .equalsIgnoreCase(studentId.trim()))
                .toList();
    }

    // Find all queue entries for a particular course.
    public synchronized List<QueueEntry> findByCourseCode(
            String courseCode) {

        if (courseCode == null || courseCode.isBlank()) {
            return List.of();
        }

        return store.readAll().stream()
                .filter(entry ->
                        entry.getCourseCode() != null
                                && entry.getCourseCode()
                                .equalsIgnoreCase(courseCode.trim()))
                .toList();
    }

    // Find waiting students for a course in queue order.
    public synchronized List<QueueEntry> findWaitingByCourseCode(
            String courseCode) {

        return findByCourseCode(courseCode).stream()
                .filter(entry ->
                        QueueEntry.WAITING.equalsIgnoreCase(
                                entry.getStatus()))
                .sorted(Comparator.comparingInt(
                        QueueEntry::getPosition))
                .toList();
    }

    // Insert a new queue entry.
    public synchronized void insert(QueueEntry entry) {

        validateQueueEntry(entry);

        List<QueueEntry> entries = store.readAll();

        boolean duplicateId = entries.stream()
                .anyMatch(existing ->
                        existing.getQueueId() != null
                                && existing.getQueueId()
                                .equalsIgnoreCase(
                                        entry.getQueueId().trim()));

        if (duplicateId) {
            throw new IllegalArgumentException(
                    "Queue entry ID already exists.");
        }

        // Prevent a student from waiting twice for the same course.
        if (QueueEntry.WAITING.equalsIgnoreCase(entry.getStatus())) {

            boolean alreadyWaiting = entries.stream()
                    .anyMatch(existing ->
                            QueueEntry.WAITING.equalsIgnoreCase(
                                    existing.getStatus())
                                    && existing.getStudentId() != null
                                    && existing.getCourseCode() != null
                                    && existing.getStudentId()
                                    .equalsIgnoreCase(entry.getStudentId())
                                    && existing.getCourseCode()
                                    .equalsIgnoreCase(entry.getCourseCode()));

            if (alreadyWaiting) {
                throw new IllegalArgumentException(
                        "Student is already waiting for this course.");
            }
        }

        entries.add(entry);
        store.writeAll(entries);
    }

    // Update an existing queue entry.
    public synchronized boolean update(QueueEntry updatedEntry) {

        validateQueueEntry(updatedEntry);

        List<QueueEntry> entries = store.readAll();

        for (int i = 0; i < entries.size(); i++) {

            String currentId = entries.get(i).getQueueId();

            if (currentId != null
                    && currentId.equalsIgnoreCase(
                    updatedEntry.getQueueId().trim())) {

                entries.set(i, updatedEntry);
                store.writeAll(entries);

                return true;
            }
        }

        return false;
    }

    // Permanently delete a queue entry.
    public synchronized boolean deleteById(String queueId) {

        if (queueId == null || queueId.isBlank()) {
            return false;
        }

        List<QueueEntry> entries = store.readAll();

        boolean removed = entries.removeIf(entry ->
                entry.getQueueId() != null
                        && entry.getQueueId()
                        .equalsIgnoreCase(queueId.trim()));

        if (removed) {
            store.writeAll(entries);
        }

        return removed;
    }

    // Validate queue entry information.
    private void validateQueueEntry(QueueEntry entry) {

        if (entry == null) {
            throw new IllegalArgumentException(
                    "Queue entry is required.");
        }

        require(entry.getQueueId(), "Queue ID");
        require(entry.getStudentId(), "Student ID");
        require(entry.getCourseCode(), "Course code");
        require(entry.getQueueDateTime(), "Queue date and time");

        String status = entry.getStatus();

        if (status == null
                || (!QueueEntry.WAITING.equalsIgnoreCase(status)
                && !QueueEntry.PROMOTED.equalsIgnoreCase(status)
                && !QueueEntry.CANCELLED.equalsIgnoreCase(status))) {

            throw new IllegalArgumentException(
                    "Invalid queue status.");
        }

        if (QueueEntry.WAITING.equalsIgnoreCase(status)
                && entry.getPosition() < 1) {

            throw new IllegalArgumentException(
                    "Waiting queue position must be at least 1.");
        }
    }

    private void require(String value, String field) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " is required.");
        }
    }

    private void require(LocalDateTime value, String field) {

        if (value == null) {
            throw new IllegalArgumentException(
                    field + " is required.");
        }
    }
}
