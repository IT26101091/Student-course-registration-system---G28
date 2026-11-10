package com.coursehub.routes;

import com.coursehub.model.Account;
import com.coursehub.repository.AccountRepository;
import com.coursehub.service.QueueService;

import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.http.NotFoundResponse;

import java.util.Map;

public class QueueRoutes {

    private final QueueService queueService;
    private final AccountRepository accountRepository;

    public QueueRoutes(
            QueueService queueService,
            AccountRepository accountRepository) {

        this.queueService = queueService;
        this.accountRepository = accountRepository;
    }

    // POST: Student joins a course waiting list.
    public void joinQueue(Context ctx) {
        handle(ctx, () -> {

            Account account = getCurrentAccount(ctx);

            if (!"STUDENT".equalsIgnoreCase(account.getRole())) {
                throw new ForbiddenResponse(
                        "Only students can join the waiting list."
                );
            }

            QueueRequest request =
                    ctx.bodyAsClass(QueueRequest.class);

            var entry = queueService.joinQueue(
                    account.getStudentId(),
                    request.getCourseCode()
            );

            ctx.status(201).json(entry);
        });
    }

    // POST: Admin adds a selected student to a course waiting list.
    public void adminJoinQueue(Context ctx) {
        handle(ctx, () -> {

            Account account = getCurrentAccount(ctx);

            if (!"ADMIN".equalsIgnoreCase(account.getRole())) {
                throw new ForbiddenResponse(
                        "Only Admins can add students to the waiting list."
                );
            }

            AdminQueueRequest request =
                    ctx.bodyAsClass(AdminQueueRequest.class);

            var entry = queueService.joinQueue(
                    request.getStudentId(),
                    request.getCourseCode()
            );

            ctx.status(201).json(entry);
        });
    }

    // GET: Admin views the waiting list for a course.
    public void getWaitingList(Context ctx) {
        handle(ctx, () -> {

            Account account = getCurrentAccount(ctx);

            if (!"ADMIN".equalsIgnoreCase(account.getRole())) {
                throw new ForbiddenResponse(
                        "Only Admins can view the full waiting list."
                );
            }

            String courseCode = ctx.pathParam("courseCode");

            ctx.json(queueService.getWaitingList(courseCode));
        });
    }

    // GET: Student checks their position in a course waiting list.
    public void getMyQueuePosition(Context ctx) {
        handle(ctx, () -> {
            Account account = getCurrentAccount(ctx);

            if (!"STUDENT".equalsIgnoreCase(account.getRole())) {
                throw new ForbiddenResponse(
                        "Only students can check their queue position."
                );
            }

            String courseCode = ctx.pathParam("courseCode");

            var entry = queueService.getStudentQueueEntry(
                    account.getStudentId(),
                    courseCode
            );

            if (entry == null) {
                throw new NotFoundResponse(
                        "You have no waiting-list record for this course."
                );
            }

            ctx.json(Map.of(
                    "courseCode", courseCode,
                    "studentId", account.getStudentId(),
                    "position", entry.getPosition(),
                    "status", entry.getStatus()
            ));
        });
    }

    // DELETE: Student cancels their waiting-list request.
    public void cancelQueueRequest(Context ctx) {
        handle(ctx, () -> {

            Account account = getCurrentAccount(ctx);

            if (!"STUDENT".equalsIgnoreCase(account.getRole())) {
                throw new ForbiddenResponse(
                        "Only students can cancel their queue requests."
                );
            }

            String courseCode = ctx.pathParam("courseCode");

            boolean cancelled = queueService.cancelQueueRequest(
                    account.getStudentId(),
                    courseCode
            );

            if (!cancelled) {
                throw new NotFoundResponse(
                        "You are not in the waiting list for this course."
                );
            }

            ctx.status(204);
        });
    }

    private Account getCurrentAccount(Context ctx) {

        String accountId = ctx.sessionAttribute("accountId");

        if (accountId == null) {
            throw new UnauthorizedResponse("Please log in.");
        }

        return accountRepository.findByAccountId(accountId)
                .orElseThrow(() ->
                        new UnauthorizedResponse(
                                "Account not found. Please log in again."
                        ));
    }

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

    public static class AdminQueueRequest {

        private String studentId;
        private String courseCode;

        public AdminQueueRequest() {
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

    public static class QueueRequest {

        private String courseCode;

        public QueueRequest() {
        }

        public String getCourseCode() {
            return courseCode;
        }

        public void setCourseCode(String courseCode) {
            this.courseCode = courseCode;
        }

    }

}