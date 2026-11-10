
package com.coursehub.util;

import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;

public final class PasswordUtil {

    // Higher work factors make hashing more expensive.
    private static final int WORK_FACTOR = 12;

    // Prevent creating instances of this utility class.
    private PasswordUtil() {
    }

    /**
     * Hashes a password before it is saved.
     */
    public static String hashPassword(String password) {

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "Password cannot be empty."
            );
        }

        if (password.length() < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters."
            );
        }

        // BCrypt supports passwords up to 72 UTF-8 bytes.
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException(
                    "Password is too long."
            );
        }

        // BCrypt generates a random salt automatically.
        return BCrypt.hashpw(
                password,
                BCrypt.gensalt(WORK_FACTOR)
        );
    }

    /**
     * Checks whether a password matches its stored hash.
     */
    public static boolean verifyPassword(
            String password,
            String storedHash
    ) {

        if (password == null
                || storedHash == null
                || storedHash.isBlank()) {
            return false;
        }

        try {
            return BCrypt.checkpw(password, storedHash);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
