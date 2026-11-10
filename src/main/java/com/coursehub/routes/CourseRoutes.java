
package com.coursehub.routes;

import com.coursehub.model.Course;
import com.coursehub.service.CourseService;
import io.javalin.http.Context;

import java.util.Map;

public class CourseRoutes {

    private final CourseService service;

    public CourseRoutes(CourseService service) {
        this.service = service;
    }

    // Get all courses
    public void getAll(Context ctx) {
        handle(ctx, () ->
                ctx.json(service.getAllCourses()));
    }

    // Get one course by course code
    public void getOne(Context ctx) {
        handle(ctx, () -> {
            String code = ctx.pathParam("courseCode");

            service.getCourseByCode(code)
                    .ifPresentOrElse(
                            ctx::json,
                            () -> ctx.status(404).json(
                                    Map.of("error", "Course not found."))
                    );
        });
    }

    // Create a new course
    public void create(Context ctx) {
        handle(ctx, () -> {
            Course course = ctx.bodyAsClass(Course.class);

            service.addCourse(course);

            ctx.status(201).json(course);
        });
    }

    // Update an existing course
    public void update(Context ctx) {
        handle(ctx, () -> {
            String code = ctx.pathParam("courseCode");
            Course course = ctx.bodyAsClass(Course.class);

            service.updateCourse(code, course);

            ctx.json(course);
        });
    }

    // Delete a course
    public void delete(Context ctx) {
        handle(ctx, () -> {
            String code = ctx.pathParam("courseCode");

            service.deleteCourse(code);

            ctx.status(204);
        });
    }

    // Handle errors
    private void handle(Context ctx, Runnable action) {
        try {
            action.run();

        } catch (IllegalStateException e) {
            ctx.status(409).json(
                    Map.of("error", e.getMessage()));

        } catch (IllegalArgumentException e) {
            ctx.status(400).json(
                    Map.of("error", e.getMessage()));
        }
    }
}
