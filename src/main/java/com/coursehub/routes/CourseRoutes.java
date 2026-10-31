
package com.coursehub.routes;

import com.coursehub.model.Course;
import com.coursehub.repository.CourseRepository;
import com.coursehub.util.CourseInsertionSort;
import io.javalin.http.Context;

import java.util.List;

public class CourseRoutes {

    private final CourseRepository repository =
            new CourseRepository();

    public void getAll(Context ctx) {
        List<Course> courses = repository.findAll();

        List<Course> sortedCourses =
                CourseInsertionSort.sortByTitle(courses);

        ctx.json(sortedCourses);
    }
}
