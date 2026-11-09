
package com.coursehub.routes;
import com.coursehub.model.Registration;
import com.coursehub.model.QueueEntry;
import com.coursehub.model.Student;
import com.coursehub.service.StudentService;
import com.coursehub.repository.AccountRepository;
import com.coursehub.repository.RegistrationRepository;
import com.coursehub.repository.QueueRepository;
import io.javalin.http.Context;

import java.util.Map;

public class StudentRoutes {

    private final StudentService service;
    private final AccountRepository accountRepository;
    private final RegistrationRepository registrationRepository;
    private final QueueRepository queueRepository;

    public StudentRoutes(
            StudentService service,
            AccountRepository accountRepository,
            RegistrationRepository registrationRepository,
            QueueRepository queueRepository
    ) {
        this.service = service;
        this.accountRepository = accountRepository;
        this.registrationRepository = registrationRepository;
        this.queueRepository = queueRepository;
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

            // Confirm that the student exists.
            service.getStudent(id);

            // Check for active registrations.
            boolean hasActiveRegistration =
                    registrationRepository.findByStudentId(id)
                            .stream()
                            .anyMatch(registration ->
                                    Registration.ACTIVE.equalsIgnoreCase(
                                            registration.getStatus()
                                    )
                            );

            if (hasActiveRegistration) {
                throw new IllegalStateException(
                        "Cannot delete student. The student has active course registrations."
                );
            }

            // Check for waiting-list entries.
            boolean isWaiting =
                    queueRepository.findByStudentId(id)
                            .stream()
                            .anyMatch(entry ->
                                    QueueEntry.WAITING.equalsIgnoreCase(
                                            entry.getStatus()
                                    )
                            );

            if (isWaiting) {
                throw new IllegalStateException(
                        "Cannot delete student. The student is currently in a waiting list."
                );
            }

            // Delete student and associated login account.
            service.deleteStudent(id);
            accountRepository.deleteByStudentId(id);

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

        }catch (IllegalStateException e) {
            ctx.status(409).json(Map.of("error", e.getMessage()));

        }catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));

        }


    }
}
