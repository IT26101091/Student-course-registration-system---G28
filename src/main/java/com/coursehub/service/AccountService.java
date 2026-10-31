package com.coursehub.service;

import com.coursehub.model.Account;
import com.coursehub.repository.AccountRepository;
import com.coursehub.util.PasswordUtil;

import java.util.Optional;
import java.util.UUID;

public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService() {
        this(new AccountRepository());
    }

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account createAdminAccount(
            String username,
            String initialPassword) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "Admin username is required.");
        }

        String normalizedUsername = username.trim();

        if (accountRepository.usernameExists(normalizedUsername)) {
            throw new IllegalArgumentException(
                    "This username already exists.");
        }

        String passwordHash =
                PasswordUtil.hashPassword(initialPassword);

        Account admin = new Account(
                "ACC-" + UUID.randomUUID(),
                normalizedUsername,
                passwordHash,
                "ADMIN",
                null,
                true
        );

        accountRepository.insert(admin);

        return admin;
    }

    // Admin uses this to create a student login account.
    // The initial username is the student's ID.
    public Account createStudentAccount(
            String studentId,
            String temporaryPassword) {

        if (studentId == null || studentId.isBlank()) {
            throw new IllegalArgumentException(
                    "Student ID is required.");
        }

        String normalizedStudentId = studentId.trim();

        if (accountRepository.usernameExists(normalizedStudentId)) {
            throw new IllegalArgumentException(
                    "An account already uses this username.");
        }

        boolean studentAlreadyHasAccount =
                accountRepository.findAll().stream()
                        .anyMatch(account ->
                                normalizedStudentId.equalsIgnoreCase(
                                        account.getStudentId()));

        if (studentAlreadyHasAccount) {
            throw new IllegalArgumentException(
                    "This student already has an account.");
        }

        String passwordHash =
                PasswordUtil.hashPassword(temporaryPassword);

        Account account = new Account(
                "ACC-" + UUID.randomUUID(),
                studentId,
                passwordHash,
                "STUDENT",
                studentId,
                true
        );

        accountRepository.insert(account);

        return account;
    }

    // Check a username and password during login.
    public Optional<Account> authenticate(
            String username,
            String password) {

        if (username == null || username.isBlank()
                || password == null) {
            return Optional.empty();
        }

        Optional<Account> result =
                accountRepository.findByUsername(username.trim());

        if (result.isEmpty()) {
            return Optional.empty();
        }

        Account account = result.get();

        if (!account.isEnabled()) {
            return Optional.empty();
        }

        if (!PasswordUtil.verifyPassword(
                password, account.getPasswordHash())) {
            return Optional.empty();
        }

        return Optional.of(account);
    }

    // Change the password and complete the first-time password change.
    public boolean changePassword(
            String accountId,
            String currentPassword,
            String newPassword) {

        Optional<Account> result =
                accountRepository.findByAccountId(accountId);

        if (result.isEmpty()) {
            return false;
        }

        Account account = result.get();

        if (!PasswordUtil.verifyPassword(
                currentPassword, account.getPasswordHash())) {
            return false;
        }

        account.setPasswordHash(
                PasswordUtil.hashPassword(newPassword));

        account.setMustChangePassword(false);

        return accountRepository.update(account);
    }

    // Change the username of an existing account.
    public boolean changeUsername(
            String accountId,
            String newUsername) {

        if (newUsername == null || newUsername.isBlank()) {
            throw new IllegalArgumentException(
                    "Username cannot be empty.");
        }

        Optional<Account> result =
                accountRepository.findByAccountId(accountId);

        if (result.isEmpty()) {
            return false;
        }

        Account account = result.get();

        account.setUsername(newUsername.trim());

        return accountRepository.update(account);
    }
}