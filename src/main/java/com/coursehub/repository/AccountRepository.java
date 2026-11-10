package com.coursehub.repository;

import com.coursehub.model.Account;
import com.fasterxml.jackson.core.type.TypeReference;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class AccountRepository {

    private final JsonFileStore<Account> store;

    // Normal application storage.
    public AccountRepository() {
        this(Path.of("data", "accounts.json"));
    }

    // Separate file path is useful for testing.
    public AccountRepository(Path filePath) {
        store = new JsonFileStore<>(
                filePath,
                new TypeReference<List<Account>>() {}
        );
    }

    // Retrieve all accounts.
    public synchronized List<Account> findAll() {
        return store.readAll();
    }

    // Find an account by username, ignoring capitalization.
    public synchronized Optional<Account> findByUsername(
            String username) {

        if (username == null || username.isBlank()) {
            return Optional.empty();
        }

        String search = username.trim();

        return store.readAll()
                .stream()
                .filter(account ->
                        account.getUsername() != null
                                && account.getUsername()
                                .equalsIgnoreCase(search))
                .findFirst();
    }

    // Find an account by its internal account ID.
    public synchronized Optional<Account> findByAccountId(
            String accountId) {

        if (accountId == null || accountId.isBlank()) {
            return Optional.empty();
        }

        String search = accountId.trim();

        return store.readAll()
                .stream()
                .filter(account ->
                        account.getAccountId() != null
                                && account.getAccountId()
                                .equalsIgnoreCase(search))
                .findFirst();
    }

    // Check whether a username is already in use.
    public synchronized boolean usernameExists(
            String username) {

        return findByUsername(username).isPresent();
    }

    // Insert a new account after checking for duplicates.
    public synchronized void insert(Account account) {

        validateAccount(account);

        List<Account> accounts = store.readAll();

        boolean duplicateUsername = accounts.stream()
                .anyMatch(existing ->
                        existing.getUsername() != null
                                && existing.getUsername()
                                .equalsIgnoreCase(
                                        account.getUsername().trim()));

        if (duplicateUsername) {
            throw new IllegalArgumentException(
                    "Username already exists: "
                            + account.getUsername());
        }

        boolean duplicateAccountId = accounts.stream()
                .anyMatch(existing ->
                        existing.getAccountId() != null
                                && existing.getAccountId()
                                .equalsIgnoreCase(
                                        account.getAccountId().trim()));

        if (duplicateAccountId) {
            throw new IllegalArgumentException(
                    "Account ID already exists: "
                            + account.getAccountId());
        }

        accounts.add(account);
        store.writeAll(accounts);
    }

    // Update an existing account.
    public synchronized boolean update(Account updatedAccount) {

        validateAccount(updatedAccount);

        List<Account> accounts = store.readAll();
        int accountIndex = -1;

        // Find the existing account.
        for (int i = 0; i < accounts.size(); i++) {
            String currentId = accounts.get(i).getAccountId();

            if (currentId != null
                    && currentId.equalsIgnoreCase(
                    updatedAccount.getAccountId().trim())) {

                accountIndex = i;
                break;
            }
        }

        if (accountIndex == -1) {
            return false;
        }

        // Do not allow another account to use this username.
        for (int i = 0; i < accounts.size(); i++) {
            if (i == accountIndex) {
                continue;
            }

            String existingUsername =
                    accounts.get(i).getUsername();

            if (existingUsername != null
                    && existingUsername.equalsIgnoreCase(
                    updatedAccount.getUsername().trim())) {

                throw new IllegalArgumentException(
                        "Username already exists: "
                                + updatedAccount.getUsername());
            }
        }

        accounts.set(accountIndex, updatedAccount);
        store.writeAll(accounts);

        return true;
    }

    // Delete an account by its internal account ID.
    public synchronized boolean deleteByAccountId(
            String accountId) {

        if (accountId == null || accountId.isBlank()) {
            return false;
        }

        List<Account> accounts = store.readAll();

        boolean removed = accounts.removeIf(account ->
                account.getAccountId() != null
                        && account.getAccountId()
                        .equalsIgnoreCase(accountId.trim()));

        if (removed) {
            store.writeAll(accounts);
        }

        return removed;
    }


    // Delete an account using its permanent student ID.
    public synchronized boolean deleteByStudentId(String studentId) {

        if (studentId == null || studentId.isBlank()) {
            return false;
        }

        List<Account> accounts = store.readAll();

        boolean removed = accounts.removeIf(account ->
                account.getStudentId() != null
                        && account.getStudentId().equalsIgnoreCase(
                        studentId.trim()));

        if (removed) {
            store.writeAll(accounts);
        }

        return removed;
    }


    private void validateAccount(Account account) {

        if (account == null) {
            throw new IllegalArgumentException(
                    "Account is required.");
        }

        require(account.getAccountId(), "Account ID");
        require(account.getUsername(), "Username");
        require(account.getPasswordHash(), "Password hash");
        require(account.getRole(), "Role");
    }

    private String require(String value, String field) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " is required.");
        }

        return value.trim();
    }
}
