package com.coursehub.service;

    public class
    StudentQueueTest {

        public static void main(String[] args) {

            StudentQueue queue = new StudentQueue();

            queue.enqueue("STU-001");
            queue.enqueue("STU-002");
            queue.enqueue("STU-003");

            System.out.println("Queue Size: " + queue.size());

            System.out.println("First Student: " + queue.peek());

            System.out.println("Removed Student: " + queue.dequeue());

            System.out.println("New First Student: " + queue.peek());

            System.out.println("Queue Empty? " + queue.isEmpty());
        }
    }


