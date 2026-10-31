
package com.coursehub.service;

public class WaitingListPersistenceTest {

    public static void main(String[] args) {

        CourseWaitingListService service =
                new CourseWaitingListService();

        String courseCode = "TEST-" + System.currentTimeMillis();

        System.out.println("Course: " + courseCode);

        System.out.println("Student 1 Added: " +
                service.joinWaitingList(courseCode, "STU-101"));

        System.out.println("Student 2 Added: " +
                service.joinWaitingList(courseCode, "STU-102"));

        System.out.println("Duplicate Added: " +
                service.joinWaitingList(courseCode, "STU-101"));

        System.out.println("Waiting Count: " +
                service.getWaitingCount(courseCode));

        CourseWaitingListService newService =
                new CourseWaitingListService();

        System.out.println("Count From New Service: " +
                newService.getWaitingCount(courseCode));

        System.out.println("First Removed: " +
                newService.getNextStudent(courseCode));

        System.out.println("Remaining Count: " +
                newService.getWaitingCount(courseCode));
    }
}
