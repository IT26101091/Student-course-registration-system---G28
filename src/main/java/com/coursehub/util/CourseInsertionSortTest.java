
package com.coursehub.util;

import com.coursehub.model.Course;
import java.util.ArrayList;
import java.util.List;

public class CourseInsertionSortTest {

    public static void main(String[] args) {

        List<Course> courses = new ArrayList<>();

        courses.add(new Course(
                "CS103", "Web Development",
                "Web Programming", 3, 30));

        courses.add(new Course(
                "CS101", "Algorithms",
                "Algorithm Design", 4, 40));

        courses.add(new Course(
                "CS102", "Database Systems",
                "Database Management", 3, 35));

        System.out.println("Before Sorting:");

        for (Course course : courses) {
            System.out.println(course.getTitle());
        }

        List<Course> sortedCourses =
                CourseInsertionSort.sortByTitle(courses);

        System.out.println("\nAfter Sorting:");

        for (Course course : sortedCourses) {
            System.out.println(course.getTitle());
        }
    }
}
