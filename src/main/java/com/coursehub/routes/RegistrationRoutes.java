
package com.coursehub.routes;

import com.coursehub.model.Account;
import com.coursehub.model.Registration;
import com.coursehub.repository.AccountRepository;
import com.coursehub.service.RegistrationService;
import com.coursehub.service.QueueService;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.NotFoundResponse;
import io.javalin.http.UnauthorizedResponse;

import java.util.Map;

public class RegistrationRoutes {

    private final RegistrationService service;
    private final AccountRepository accountRepository;
    private final QueueService queueService;

    public RegistrationRoutes(
            RegistrationService service,
            AccountRepository accountRepository,
            QueueService queueService) {

        this.service = service;
        this.accountRepository = accountRepository;
        this.queueService = queueService;
    }

    // GET: Retrieve registrations.
    // Admin sees all; Student sees their own.
    public void getAll(Context ctx) {
        handle(ctx, () -> {
            Account account = getCurrentAccount(ctx);

            if (isAdmin(account)) {
                ctx.json(service.getAllRegistrations());
            } else {
                ctx.json(service.getRegistrationsByStudent(
                        account.getStudentId()));
            }
        });
    }

    // GET: Retrieve registrations sorted by registration time.
    public void getSortedByTime(Context ctx) {
        handle(ctx, () -> {
            Account account = getCurrentAccount(ctx);

            if (isAdmin(account)) {
                ctx.json(service.getRegistrationsSortedByTime());
            } else {
                ctx.json(service.getStudentRegistrationsSortedByTime(
                        account.getStudentId()));
            }
        });
    }

    // GET: Retrieve one registration.
    public void getOne(Context ctx) {
        handle(ctx, () -> {
            Account account = getCurrentAccount(ctx);

            String registrationId =
                    ctx.pathParam("registrationId");

            Registration registration =
                    service.getRegistrationById(registrationId)
                            .orElseThrow(() ->
                                    new NotFoundResponse(
                                            "Registration not found."));

            checkOwnership(account, registration);

            ctx.json(registration);
        });
    }

    // GET: Retrieve registrations for a student.
    // Admin can view any student; Student can view only themselves.
    public void getByStudent(Context ctx) {
        handle(ctx, () -> {
            Account account = getCurrentAccount(ctx);

            String studentId = ctx.pathParam("studentId");

            if (!isAdmin(account)
                    && !sameId(account.getStudentId(), studentId)) {
                throw new ForbiddenResponse(
                        "You can only view your own registrations.");
            }

            ctx.json(service.getRegistrationsByStudent(studentId));
        });
    }

    // GET: Retrieve registrations for a course (Admin only).
    public void getByCourse(Context ctx) {
        handle(ctx, () -> {
            requireAdmin(getCurrentAccount(ctx));

            String courseCode = ctx.pathParam("courseCode");

            ctx.json(service.getRegistrationsByCourse(courseCode));
        });
    }

    // POST: Register a student for a course.
    public void create(Context ctx) {
        handle(ctx, () -> {
            Account account = getCurrentAccount(ctx);

            RegisterRequest request =
                    ctx.bodyAsClass(RegisterRequest.class);

            String studentId;

            if (isAdmin(account)) {
                studentId = request.getStudentId();
            } else {
                // Students cannot register on behalf of another student.
                studentId = account.getStudentId();
            }

            Registration registration =
                    service.registerStudentForCourse(
                            studentId,
                            request.getCourseCode());

            ctx.status(201).json(registration);
        });
    }

    // DELETE: Student cancels their registration.
    // Admin permanently deletes the registration.
    public void delete(Context ctx) {
        handle(ctx, () -> {
            Account account = getCurrentAccount(ctx);

            String registrationId =
                    ctx.pathParam("registrationId");

            Registration registration =
                    service.getRegistrationById(registrationId)
                            .orElseThrow(() ->
                                    new NotFoundResponse(
                                            "Registration not found."));

            if (isAdmin(account)) {

                // Check whether the registration is currently active.
                boolean wasActive = Registration.ACTIVE.equalsIgnoreCase(
                        registration.getStatus()
                );

                // Permanently delete the registration.
                service.deleteRegistration(registrationId);

                // If an active seat was freed, promote the next student.
                if (wasActive) {
                    queueService.promoteNextStudent(registration.getCourseCode());
                }

                ctx.status(204);
            } else {
                checkOwnership(account, registration);

                Registration cancelled = service.dropCourse(
                        account.getStudentId(),
                        registration.getCourseCode()
                );

// Give the freed seat to the next student in the queue.
                queueService.promoteNextStudent(registration.getCourseCode());

                ctx.json(cancelled);
            }
        });
    }

    // PUT: Admin changes registration status.
    public void changeStatus(Context ctx) {
        handle(ctx, () -> {
            requireAdmin(getCurrentAccount(ctx));

            String registrationId =
                    ctx.pathParam("registrationId");

            StatusRequest request =
                    ctx.bodyAsClass(StatusRequest.class);

            // Get the existing registration.
            Registration existing = service.getRegistrationById(registrationId)
                    .orElseThrow(() ->
                            new NotFoundResponse("Registration not found."));

            // Check whether it was previously active.
            boolean wasActive = Registration.ACTIVE.equalsIgnoreCase(
                    existing.getStatus()
            );

            // Change the registration status.
            Registration updated = service.changeRegistrationStatus(
                    registrationId,
                    request.getStatus()
            );

            // If an active registration was cancelled,
            // promote the next student in the waiting list.
            if (wasActive &&
                    Registration.CANCELLED.equalsIgnoreCase(updated.getStatus())) {

                queueService.promoteNextStudent(updated.getCourseCode());
            }

            ctx.json(updated);
        });
    }

    // Get the currently logged-in account.
    private Account getCurrentAccount(Context ctx) {

        String accountId = ctx.sessionAttribute("accountId");

        if (accountId == null) {
            throw new UnauthorizedResponse("Please log in.");
        }

        return accountRepository.findByAccountId(accountId)
                .orElseThrow(() ->
                        new UnauthorizedResponse(
                                "Account not found. Please log in again."));
    }

    private boolean isAdmin(Account account) {
        return "ADMIN".equalsIgnoreCase(account.getRole());
    }

    private void requireAdmin(Account account) {
        if (!isAdmin(account)) {
            throw new ForbiddenResponse(
                    "This action is available to Admins only.");
        }
    }

    private void checkOwnership(
            Account account,
            Registration registration) {

        if (!isAdmin(account)
                && !sameId(
                account.getStudentId(),
                registration.getStudentId())) {

            throw new ForbiddenResponse(
                    "You cannot access another student's registration.");
        }
    }

    private boolean sameId(String first, String second) {
        return first != null
                && second != null
                && first.equalsIgnoreCase(second);
    }

    // Common error handling.
    private void handle(Context ctx, Runnable action) {
        try {
            action.run();

        } catch (NotFoundResponse e) {
            ctx.status(404).json(Map.of("error", e.getMessage()));

        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));

        } catch (IllegalStateException e) {
            ctx.status(409).json(Map.of("error", e.getMessage()));
        }
    }

    // Request body for creating a registration.
    public static class RegisterRequest {
        private String studentId;
        private String courseCode;

        public RegisterRequest() {
        }

        public String getStudentId() {
            return studentId;
        }

        public void setStudentId(String studentId) {
            this.studentId = studentId;
        }

        public String getCourseCode() {
            return courseCode;
        }

        public void setCourseCode(String courseCode) {
            this.courseCode = courseCode;
        }
    }

    // Request body for changing registration status.
    public static class StatusRequest {
        private String status;

        public StatusRequest() {
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
