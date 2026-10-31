
package com.coursehub.service;

public class QueueDuplicateTest {

    public static void main(String[] args) {

        StudentQueue queue = new StudentQueue();

        System.out.println(queue.enqueue("STU-001"));
        System.out.println(queue.enqueue("STU-002"));
        System.out.println(queue.enqueue("STU-001"));

        System.out.println("Queue Size: " + queue.size());
        System.out.println("First Student: " + queue.peek());
    }
}
