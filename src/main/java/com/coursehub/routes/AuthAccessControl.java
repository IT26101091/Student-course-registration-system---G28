package com.coursehub.routes;

import com.coursehub.model.Account;
import com.coursehub.repository.AccountRepository;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.UnauthorizedResponse;

public class AuthAccessControl {

    private final AccountRepository accountRepository;

    public AuthAccessControl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public void checkAccess(Context ctx) {

        String path = ctx.path();
        String httpMethod = ctx.method().name();

        // Public endpoints.
        if (path.equals("/api/health")
                || path.equals("/api/auth/login")
                || path.equals("/api/auth/logout")) {
            return;
        }

        // Every other API endpoint requires a valid session.
        String accountId = ctx.sessionAttribute("accountId");

        if (accountId == null || accountId.isBlank()) {
            throw new UnauthorizedResponse("Please log in.");
        }

        Account account = accountRepository
                .findByAccountId(accountId)
                .orElse(null);

        if (account == null || !account.isEnabled()) {
            var session = ctx.req().getSession(false);

            if (session != null) {
                session.invalidate();
            }

            throw new UnauthorizedResponse("Please log in again.");
        }

        String role = account.getRole() == null
                ? ""
                : account.getRole();

        boolean isAdmin = "ADMIN".equalsIgnoreCase(role);

        // First-time users can only check their account,
        // change their password, or log out.
        boolean allowedDuringPasswordChange =
                path.equals("/api/auth/me")
                        || path.equals("/api/auth/change-password");

        if (account.isMustChangePassword()
                && !allowedDuringPasswordChange) {
            throw new ForbiddenResponse(
                    "Change your temporary password before continuing.");
        }

        // Admin-only API areas.
        boolean adminOnlyPath =
                path.equals("/api/admin")
                        || path.startsWith("/api/admin/")
                        || path.equals("/api/queue")
                        || path.startsWith("/api/queue/");

        if (adminOnlyPath && !isAdmin) {
            throw new ForbiddenResponse(
                    "This action is available to Admins only.");
        }

        // Student CRUD management is Admin-only.
        boolean studentPath =
                path.equals("/api/students")
                        || path.startsWith("/api/students/");

        if (studentPath && !isAdmin) {

            String requestedStudentId = null;

            if (path.startsWith("/api/students/")) {
                requestedStudentId =
                        path.substring("/api/students/".length());

                // Don't treat nested routes as student IDs.
                if (requestedStudentId.isBlank()
                        || requestedStudentId.contains("/")) {
                    requestedStudentId = null;
                }
            }

            boolean viewingOwnRecord =
                    "GET".equalsIgnoreCase(httpMethod)
                            && "STUDENT".equalsIgnoreCase(role)
                            && requestedStudentId != null
                            && account.getStudentId() != null
                            && requestedStudentId.equalsIgnoreCase(
                            account.getStudentId());

            if (!viewingOwnRecord) {
                throw new ForbiddenResponse(
                        "You cannot manage student records.");
            }
        }

        // Only Admins can create, edit, or delete courses.
        boolean coursePath =
                path.equals("/api/courses")
                        || path.startsWith("/api/courses/");

        boolean modifyingRequest =
                "POST".equalsIgnoreCase(httpMethod)
                        || "PUT".equalsIgnoreCase(httpMethod)
                        || "PATCH".equalsIgnoreCase(httpMethod)
                        || "DELETE".equalsIgnoreCase(httpMethod);

        if (coursePath && modifyingRequest && !isAdmin) {
            throw new ForbiddenResponse(
                    "Only Admins can modify courses.");
        }
    }
}