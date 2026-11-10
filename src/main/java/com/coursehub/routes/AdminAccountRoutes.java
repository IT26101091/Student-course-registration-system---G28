package com.coursehub.routes;

import com.coursehub.model.Account;
import com.coursehub.repository.StudentRepository;
import com.coursehub.service.AccountService;
import io.javalin.http.Context;

import java.util.Map;

public class AdminAccountRoutes {

    private final AccountService accountService;
    private final StudentRepository studentRepository;

    public AdminAccountRoutes(
            AccountService accountService,
            StudentRepository studentRepository) {

        this.accountService = accountService;
        this.studentRepository = studentRepository;
    }

    // POST /api/admin/accounts
    public void createStudentAccount(Context ctx) {

        CreateAccountRequest request =
                ctx.bodyAsClass(CreateAccountRequest.class);

        if (request.getStudentId() == null
                || request.getStudentId().isBlank()) {

            ctx.status(400).json(
                    Map.of("error", "Student ID is required."));
            return;
        }

        if (request.getTemporaryPassword() == null
                || request.getTemporaryPassword().isBlank()) {

            ctx.status(400).json(
                    Map.of("error", "Temporary password is required."));
            return;
        }

        String studentId = request.getStudentId().trim();

        // A student record must exist before creating its account.
        if (studentRepository.findById(studentId).isEmpty()) {

            ctx.status(404).json(
                    Map.of("error",
                            "Create the student record first."));
            return;
        }

        try {
            Account account =
                    accountService.createStudentAccount(
                            studentId,
                            request.getTemporaryPassword());

            // Never return the password hash.
            ctx.status(201).json(Map.of(
                    "accountId", account.getAccountId(),
                    "username", account.getUsername(),
                    "role", account.getRole(),
                    "studentId", account.getStudentId(),
                    "mustChangePassword",
                    account.isMustChangePassword()
            ));

        } catch (IllegalArgumentException exception) {

            String message = exception.getMessage();

            boolean conflict = message != null
                    && (message.toLowerCase().contains("already exists")
                    || message.toLowerCase().contains(
                    "already has an account"));

            ctx.status(conflict ? 409 : 400).json(
                    Map.of("error",
                            message == null
                                    ? "Could not create account."
                                    : message));
        }
    }

    public static class CreateAccountRequest {

        private String studentId;
        private String temporaryPassword;

        public CreateAccountRequest() {
        }

        public String getStudentId() {
            return studentId;
        }

        public void setStudentId(String studentId) {
            this.studentId = studentId;
        }

        public String getTemporaryPassword() {
            return temporaryPassword;
        }

        public void setTemporaryPassword(String temporaryPassword) {
            this.temporaryPassword = temporaryPassword;
        }
    }
}