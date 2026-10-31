package com.coursehub.util;

import com.coursehub.model.Course;
import java.util.ArrayList;
import java.util.List;

public class CourseInsertionSort {

    public static List<Course> sortByTitle(List<Course> courses) {

        List<Course> sorted = new ArrayList<>(courses);

        for (int i = 1; i < sorted.size(); i++) {

            Course key = sorted.get(i);
            int j = i - 1;

            while (j >= 0 &&
                    sorted.get(j).getTitle()
                            .compareToIgnoreCase(key.getTitle()) > 0) {

                sorted.set(j + 1, sorted.get(j));
                j--;
            }

            sorted.set(j + 1, key);
        }

        return sorted;
    }
}
