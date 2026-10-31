
package com.coursehub.service;

public class CourseWaitingListTest {

    public static void main(String[] args) {

        CourseWaitingListService service =
                new CourseWaitingListService();

        service.joinWaitingList("CS101", "STU-001");
        service.joinWaitingList("CS101", "STU-002");
        service.joinWaitingList("CS101", "STU-003");

        service.joinWaitingList("CS102", "STU-004");
        service.joinWaitingList("CS102", "STU-005");

        System.out.println("CS101 Waiting Count: "
                + service.getWaitingCount("CS101"));

        System.out.println("CS102 Waiting Count: "
                + service.getWaitingCount("CS102"));

        System.out.println("Next Student CS101: "
                + service.getNextStudent("CS101"));

        System.out.println("Next Student CS102: "
                + service.getNextStudent("CS102"));

        System.out.println("CS101 Remaining: "
                + service.getWaitingCount("CS101"));

        System.out.println("CS102 Remaining: "
                + service.getWaitingCount("CS102"));
    }
}
