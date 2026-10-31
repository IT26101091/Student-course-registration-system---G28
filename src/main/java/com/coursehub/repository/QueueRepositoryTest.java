
package com.coursehub.repository;

public class QueueRepositoryTest {

    public static void main(String[] args) {

        QueueRepository repository = new QueueRepository();

        String testCourse = "TEST-" + System.currentTimeMillis();

        System.out.println("Student 1: "
                + repository.add(testCourse, "STU-001"));

        System.out.println("Student 2: "
                + repository.add(testCourse, "STU-002"));

        System.out.println("Duplicate: "
                + repository.add(testCourse, "STU-001"));

        System.out.println("Waiting Count: "
                + repository.count(testCourse));

        var first = repository.removeFirst(testCourse);
        System.out.println("Removed Student: "
                + (first == null ? "None" : first.getStudentId()));

        System.out.println("Remaining Count: "
                + repository.count(testCourse));

        var second = repository.removeFirst(testCourse);
        System.out.println("Next Student: "
                + (second == null ? "None" : second.getStudentId()));

        System.out.println("Final Count: "
                + repository.count(testCourse));
    }
}
