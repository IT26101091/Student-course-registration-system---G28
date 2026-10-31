
package com.coursehub.service;

import java.util.LinkedList;
import java.util.Queue;

public class StudentQueue {

    private final Queue<String> waitingQueue = new LinkedList<>();


    public boolean enqueue(String studentId) {

        if (studentId == null || studentId.isBlank()) {
            return false;
        }

        if (waitingQueue.contains(studentId)) {
            return false;
        }

        waitingQueue.add(studentId);
        return true;
    }

    public String dequeue() {
        return waitingQueue.poll();
    }


    public String peek() {
        return waitingQueue.peek();
    }


    public boolean isEmpty() {
        return waitingQueue.isEmpty();
    }


    public int size() {
        return waitingQueue.size();
    }
}
