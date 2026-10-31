
package com.coursehub.routes;

import com.coursehub.model.QueueEntry;
import com.coursehub.repository.QueueRepository;
import com.coursehub.service.CourseWaitingListService;
import io.javalin.http.Context;

import java.util.List;
import java.util.Map;

public class QueueRoutes {

    private final QueueRepository repository = new QueueRepository();
    private final CourseWaitingListService service =
            new CourseWaitingListService();

    // View the waiting queue for a course
    public void getByCourse(Context ctx) {
        String courseCode = ctx.pathParam("courseCode");

        List<QueueEntry> entries =
                repository.findByCourse(courseCode);

        ctx.json(entries);
    }

    // View the next student without removing them
    public void peekNext(Context ctx) {
        String courseCode = ctx.pathParam("courseCode");
        String studentId = service.peekNextStudent(courseCode);

        if (studentId == null) {
            ctx.status(404).json(
                    Map.of("error", "The waiting queue is empty.")
            );
            return;
        }

        ctx.json(Map.of("studentId", studentId));
    }

    // Remove the next student from the queue
    public void processNext(Context ctx) {
        String courseCode = ctx.pathParam("courseCode");
        String studentId = service.removeNextStudent(courseCode);

        if (studentId == null) {
            ctx.status(404).json(
                    Map.of("error", "The waiting queue is empty.")
            );
            return;
        }

        ctx.json(Map.of(
                "message", "Student removed from waiting queue.",
                "studentId", studentId
        ));
    }
}
