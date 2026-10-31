package com.coursehub.routes;

import com.coursehub.model.Account;
import com.coursehub.repository.AccountRepository;
import com.coursehub.service.AccountService;
import io.javalin.http.Context;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class AuthRoutes {

    private final AccountService accountService;
    private final AccountRepository accountRepository;

    public AuthRoutes(
            AccountService accountService,
            AccountRepository accountRepository) {

        this.accountService = accountService;
        this.accountRepository = accountRepository;
    }

    // POST /api/auth/login
    public void login(Context ctx) {

        LoginRequest request =
                ctx.bodyAsClass(LoginRequest.class);

        Optional<Account> result =
                accountService.authenticate(
                        request.getUsername(),
                        request.getPassword());

        if (result.isEmpty()) {
            ctx.status(401).json(
                    Map.of("error",
                            "Invalid username or password."));
            return;
        }

        Account account = result.get();

        // Invalidate an old session to reduce session-fixation risk.
        var oldSession = ctx.req().getSession(false);

        if (oldSession != null) {
            oldSession.invalidate();
        }

        // Store only the account ID in the session.
        ctx.sessionAttribute(
                "accountId", account.getAccountId());

        ctx.json(publicAccount(account));
    }

    // POST /api/auth/logout
    public void logout(Context ctx) {

        var session = ctx.req().getSession(false);

        if (session != null) {
            session.invalidate();
        }

        ctx.status(204);
    }

    // GET /api/auth/me
    public void currentAccount(Context ctx) {

        Account account = getLoggedInAccount(ctx);

        if (account == null) {
            ctx.status(401).json(
                    Map.of("error", "Please log in."));
            return;
        }

        ctx.json(publicAccount(account));
    }

    // POST /api/auth/change-password
    public void changePassword(Context ctx) {

        Account account = getLoggedInAccount(ctx);

        if (account == null) {
            ctx.status(401).json(
                    Map.of("error", "Please log in."));
            return;
        }

        ChangePasswordRequest request =
                ctx.bodyAsClass(ChangePasswordRequest.class);

        try {
            boolean changed =
                    accountService.changePassword(
                            account.getAccountId(),
                            request.getCurrentPassword(),
                            request.getNewPassword());

            if (!changed) {
                ctx.status(400).json(
                        Map.of("error",
                                "Current password is incorrect."));
                return;
            }

            ctx.json(Map.of(
                    "message", "Password changed successfully.",
                    "mustChangePassword", false
            ));

        } catch (IllegalArgumentException exception) {
            ctx.status(400).json(
                    Map.of("error", exception.getMessage()));
        }
    }

    // POST /api/auth/change-username
    public void changeUsername(Context ctx) {

        Account account = getLoggedInAccount(ctx);

        if (account == null) {
            ctx.status(401).json(
                    Map.of("error", "Please log in."));
            return;
        }

        ChangeUsernameRequest request =
                ctx.bodyAsClass(ChangeUsernameRequest.class);

        // Verify the current password before changing the username.
        if (accountService.authenticate(
                account.getUsername(),
                request.getCurrentPassword()).isEmpty()) {

            ctx.status(400).json(
                    Map.of("error", "Current password is incorrect."));
            return;
        }

        try {
            accountService.changeUsername(
                    account.getAccountId(),
                    request.getNewUsername());

            Account updated = accountRepository
                    .findByAccountId(account.getAccountId())
                    .orElseThrow();

            ctx.json(publicAccount(updated));

        } catch (IllegalArgumentException exception) {
            String message = exception.getMessage();

            int status = message != null
                    && message.toLowerCase().contains("already exists")
                    ? 409 : 400;

            ctx.status(status).json(
                    Map.of("error", message));
        }
    }

    // Find the account identified by the current session.
    private Account getLoggedInAccount(Context ctx) {

        String accountId =
                ctx.sessionAttribute("accountId");

        if (accountId == null) {
            return null;
        }

        Account account = accountRepository
                .findByAccountId(accountId)
                .orElse(null);

        if (account == null || !account.isEnabled()) {
            var session = ctx.req().getSession(false);

            if (session != null) {
                session.invalidate();
            }

            return null;
        }

        return account;
    }

    // Never send the password hash to the frontend.
    private Map<String, Object> publicAccount(Account account) {

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("accountId", account.getAccountId());
        response.put("username", account.getUsername());
        response.put("role", account.getRole());
        response.put("studentId", account.getStudentId());
        response.put(
                "mustChangePassword",
                account.isMustChangePassword());

        return response;
    }

    // Request body for login.
    public static class LoginRequest {
        private String username;
        private String password;

        public LoginRequest() {}

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    // Request body for changing a password.
    public static class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;

        public ChangePasswordRequest() {}

        public String getCurrentPassword() {
            return currentPassword;
        }

        public void setCurrentPassword(String currentPassword) {
            this.currentPassword = currentPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }

    // Request body for changing a username.
    public static class ChangeUsernameRequest {
        private String currentPassword;
        private String newUsername;

        public ChangeUsernameRequest() {}

        public String getCurrentPassword() {
            return currentPassword;
        }

        public void setCurrentPassword(String currentPassword) {
            this.currentPassword = currentPassword;
        }

        public String getNewUsername() {
            return newUsername;
        }

        public void setNewUsername(String newUsername) {
            this.newUsername = newUsername;
        }
    }
}