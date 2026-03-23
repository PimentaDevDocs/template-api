package com.pimentadesenvolvimento.conroledebolsaoback.config;

/**
 * Central place for all user-facing message strings.
 * Future localization can switch to resource bundles or similar.
 */
public final class Messages {
    private Messages() {
    }

    public static final class Security {
        public static final String JWT_SECRET_INVALID = "Invalid or tampered token.";
        public static final String JWT_SECRET_EXPIRED = "Authentication token expired.";
        public static final String JWT_TOKEN_BLACKLISTED = "Token has been revoked.";
        public static final String JWT_SECRET_FAILURE = "Failed to process authentication token: ";
        public static final String ACCESS_TOKEN_COOKIE = "ACCESS_TOKEN";
        public static final String REFRESH_TOKEN_COOKIE = "REFRESH_TOKEN";
        public static final String JWT_SECRET_MISSING =
                "JWT secret is not configured. Set the JWT_SECRET environment variable with a strong value (at least 32 characters).";
        public static final String JWT_SECRET_WEAK =
                "JWT secret is too weak. Use a secret with at least 32 random characters.";
        public static final String WEBHOOK_SECRET_MISSING =
                "Webhook secret is not configured. Set the WEBHOOK_SECRET environment variable.";
        public static final String WEBHOOK_SIGNATURE_MISSING =
                "Missing webhook signature header.";
        public static final String WEBHOOK_SIGNATURE_INVALID =
                "Invalid webhook signature.";
        public static final String WEBHOOK_SIGNATURE_INTERNAL_ERROR =
                "Internal error during signature validation.";
        public static final String LOGIN_RATE_LIMIT_EXCEEDED =
                "Too many login attempts. Please try again in a few moments.";
        public static final String USER_NOT_AUTHENTICATED =
                "User is not authenticated in security context.";
        public static final String LOGGED_USER_NOT_FOUND =
                "Logged user '%s' not found in the database.";

        private Security() {
        }
    }

    public static final class Validation {
        public static final String USERNAME_SIZE = "Username must be between 3 and 50 characters.";
        public static final String USERNAME_PATTERN = "Username can only contain letters, numbers, and underscores.";
        public static final String PASSWORD_REQUIRED = "Password is required.";
        public static final String USERNAME_REQUIRED = "Username is required.";
        public static final String PASSWORD_MIN = "Password must be at least 6 characters long.";
        public static final String EMAIL_REQUIRED = "Email is required.";
        public static final String EMAIL_INVALID = "Invalid email.";
        public static final String PASSWORD_MISSING = "Password not provided.";
        public static final String PASSWORD_MIN_STRONG = "Password must be at least 10 characters long.";
        public static final String PASSWORD_MAX_STRONG = "Password must be no more than 128 characters long.";
        public static final String PASSWORD_COMPLEXITY =
                "Password must include uppercase, lowercase, number, and special character.";
        public static final String PASSWORD_TOO_COMMON =
                "Provided password is too common. Choose a stronger one.";

        private Validation() {
        }
    }

    public static final class Errors {
        public static final String INVALID_CREDENTIALS =
                "Invalid credentials. Check your username and password.";
        public static final String SERVER_ERROR = "An unexpected server error occurred.";
        public static final String VALIDATION_FAILED = "Failed to validate submitted data.";
        public static final String RESOURCE_NOT_FOUND = "Resource not found.";

        private Errors() {
        }
    }

    public static final class Person {
        public static final String PERSON_NOT_FOUND_BY_ID = "Person not found with ID: %d";

        private Person() {
        }
    }

    public static final class UserService {
        public static final String USER_NOT_FOUND_BY_ID = "User not found with ID: %d";
        public static final String USERNAME_NOT_FOUND = "User not found: %s";
        public static final String USERNAME_ALREADY_IN_USE = "Username already in use";
        public static final String ERROR_RELOADING_UPDATED = "Error reloading updated user";
        public static final String ERROR_RELOADING_CREATED = "Error reloading created user";
        public static final String ONLY_ADMINS_VIEW = "Only administrators can view full details";
        public static final String ONLY_ADMINS_EDIT = "Only administrators can edit users";

        private UserService() {
        }
    }

    public static final class DataLoader {
        public static final String START_LOADING = "Starting default data load (roles and admin)...";
        public static final String LOAD_FINISHED = "Data load completed successfully.";
        public static final String ADMIN_CREATED = "Admin user created successfully.";
        public static final String ADMIN_EXISTS = "Admin user already exists, skipping creation.";
        public static final String DEFAULT_ADMIN_PASSWORD_COMMENT =
                "Default password for first access";

        private DataLoader() {
        }
    }
}
