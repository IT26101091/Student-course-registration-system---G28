
package com.coursehub.routes;

import com.coursehub.model.Student;
import com.coursehub.service.StudentService;
import io.javalin.http.Context;

import java.util.Map;

public class StudentRoutes {

    private final StudentService service;

    public StudentRoutes(StudentService service) {
        this.service = service;
    }

    public void getAll(Context ctx) {
        handle(ctx, () ->
                ctx.json(service.getAllStudents()));
    }

    public void getOne(Context ctx) {
        handle(ctx, () -> {
            String id = ctx.pathParam("studentId");
            ctx.json(service.getStudent(id));
        });
    }

    public void create(Context ctx) {
        handle(ctx, () -> {
            Student student = ctx.bodyAsClass(Student.class);

            Student created = service.createStudent(student);

            ctx.status(201).json(created);
        });
    }

    public void update(Context ctx) {
        handle(ctx, () -> {
            String id = ctx.pathParam("studentId");
            Student student = ctx.bodyAsClass(Student.class);

            ctx.json(service.updateStudent(id, student));
        });
    }

    public void delete(Context ctx) {
        handle(ctx, () -> {
            String id = ctx.pathParam("studentId");

            service.deleteStudent(id);
            ctx.status(204);
        });
    }

    private void handle(Context ctx, Runnable action) {
        try {
            action.run();

        } catch (StudentService.StudentNotFoundException e) {
            ctx.status(404).json(Map.of("error", e.getMessage()));

        } catch (StudentService.DuplicateStudentException e) {
            ctx.status(409).json(Map.of("error", e.getMessage()));

        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        }
    }
}
