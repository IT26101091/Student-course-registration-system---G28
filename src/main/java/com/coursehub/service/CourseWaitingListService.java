package com.coursehub.service;

import com.coursehub.model.QueueEntry;
import com.coursehub.repository.QueueRepository;

public class CourseWaitingListService {

    private final QueueRepository repository = new QueueRepository();

    public boolean joinWaitingList(String courseCode, String studentId) {
        return repository.add(courseCode, studentId);
    }


    public String peekNextStudent(String courseCode) {
        if (courseCode == null || courseCode.isBlank()) {
            return null;
        }

        QueueEntry entry = repository.peekFirst(courseCode);

        if (entry == null) {
            return null;
        }

        return entry.getStudentId();
    }


    public String removeNextStudent(String courseCode) {
        if (courseCode == null || courseCode.isBlank()) {
            return null;
        }

        QueueEntry entry = repository.removeFirst(courseCode);

        if (entry == null) {
            return null;
        }

        return entry.getStudentId();
    }


    public String getNextStudent(String courseCode) {
        return removeNextStudent(courseCode);
    }

    public int getWaitingCount(String courseCode) {
        if (courseCode == null || courseCode.isBlank()) {
            return 0;
        }

        return repository.count(courseCode);
    }
}