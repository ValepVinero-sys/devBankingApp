package com.Bank;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
/**
 * DTO for requesting new user registration.
 * <p>
 * Used in the {@code POST /api/auth/register} endpoint.
 * Contains all the necessary information to create an account.
 * </p>
 *
 * <p><b>Request lifecycle:</b></p>
 *  <ol>
 *  <li>The client sends JSON with user data</li>
 *  <li>Spring validates all fields ({@code @Valid} annotations)</li>
 *  <li>If validation is successful, a new {@link User} is created</li>
 *  <li>The password is encrypted using BCrypt</li>
 *  <li>The user is saved to the database</li>
 *  </ol>
 * </p>
 *
 * <p><b>Example JSON request:</b></p>
 *  <pre>
 *  {
 *  "firstName": "Ivan",
 *  "lastName": "Ivanov",
 *  "email": "ivan.ivanov@example.com",
 *  "password": "SecurePass123!",
 *  "phoneNumber": "+7 (999) 123-45-67" // optional
 *  }
 *  </pre>
 *
 * <p><b>Example of a successful response (200 OK):</b></p>
 * <pre>
 * {
 * "message": "User successfully registered",
 * "email": "ivan.ivanov@example.com"
 * }
 * </pre>
 *
 * <p><b>Error example (409 Conflict):</b></p>
 * <pre>
 * {
 * "timestamp": "2025-01-15T10:30:00",
 * "status": 409,
 * "error": "Conflict",
 * "message": "User with email ivan.ivanov@example.com already exists"
 * }
 * </pre>
 *
 * @author Valep Vinreo
 * @version 1.0
 * @see LoginRequest
 * @see User
 * @see AuthController#registerUser(RegisterRequest)
 * @since 04-2026
 */
@Data
public class RegisterRequest {
    /**
     * Username.
     * <p>
     * <b>Requirements:</b>
     * <ul>
     * <li>Required field</li>
     * <li>Length from 1 to 50 characters</li>
     * <li>Only letters (Cyrillic or Latin), hyphens, and spaces</li>
     * <li>The first letter is usually capitalized</li>
     * </ul>
     * </p>
     *
     * <p><b>Examples:</b></p>
     * <ul>
     * <li>Ivan</li>
     * <li>Anna-Maria</li>
     * <li>John</li>
     * </ul>
     *
     * @see #lastName
     */
    @NotBlank(message = "First name is required")
    private String firstName;
    /**
     * User's last name.
     * <p>
     * <b>Requirements:</b>
     * <ul>
     * <li>Required field</li>
     * <li>Length from 1 to 50 characters</li>
     * <li>Only letters (Cyrillic or Latin), hyphen and space</li>
     * <li>The first letter is usually capitalized</li>
     * </ul>
     * </p>
     *
     * <p><b>Examples:</b></p>
     * <ul>
     * <li>Ivanov</li>
     * <li>Petrova-Sidorova</li>
     * <li>Smith</li>
     * </ul>
     *
     * @see #firstName
     */
    @NotBlank(message = "Last name is required")
    private String lastName;
    /**
     * Email address (unique user identifier).
     * <p>
     * <b>Requirements:</b>
     * <ul>
     * <li>Required field</li>
     * <li>Unique throughout the system</li>
     * <li>Correct email format</li>
     * <li>Used as login</li>
     * <li>Case-sensitive</li>
     * </ul>
     * </p>
     *
     * <p><b>Email validation:</b></p>
     * <pre>
     * V user@example.com (correct)
     * V name.lastname@domain.com
     * V user+tag@example.com (plus allowed)
     * X user@ (no domain)
     * X user@domain (no dot)
     * X @domain.com (no name)
     * </pre>
     *
     * <p><b>Checks after Sending:</b></p>
     * <ul>
     * <li>Check for uniqueness in the database</li>
     * <li>If the email already exists → error 409 Conflict</li>
     * </ul>
     *
     * @see User#getEmail()
     * @see #password
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    /**
     * User password.
     * <p>
     * <b>Password requirements:</b>
     * <ul>
     * <li>Required field</li>
     * <li>Minimum length: 6 characters</li>
     * <li>Maximum length: 100 characters</li>
     * <li>Complex passwords are recommended</li>
     * </ul>
     * </p>
     *
     * <p><b>Security Recommendations:</b></p>
     * <ul>
     * <li>Minimum 8 characters (the longer, the better)</li>
     * <li>Use mixed-case characters (A-Z, a-z)</li>
     * <li>Add numbers (0-9)</li>
     * <li>Add special characters (!@#$%^&*)</li>
     * <li>Avoid personal information</li>
     * <li>Avoid common passwords (password, 123456)</li>
     * </ul>
     * </p>
     *
     * <p><b>Examples of strong passwords:</b></p>
     * <pre>
     * MySecureP@ssw0rd2025!
     * XyZ!9876AbCd
     * Blue$ky42#Cloud
     * </pre>
     *
     * <p><b> Important:</b></p>
     * <ul>
     * <li>The password is transmitted via HTTPS (encrypted)</li>
     * <li>Only the hash (BCrypt) is stored in the database</li>
     * <li>The original password is NOT saved</li>
     * <li>Even the administrator CANNOT find out the password</li>
     * </ul>
     *
     * @see org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
     * @see User#getPassword()
     */
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    /**
     * (Optional) The user's phone number.
     * <p>
     * <b>Format:</b>
     * <ul>
     * <li>Optional field (can be null)</li>
     * <li>Accepts the following formats: +7 (999) 123-45-67, 89991234567</li>
     * <li>Used for two-factor authentication</li>
     * </ul>
     * </p>
     *
     * <p><b>Examples:</b></p>
     * <ul>
     * <li>+7 (999) 123-45-67</li>
     * <li>89991234567</li>
     * <li>+1 (555) 123-4567</li>
     * </ul>
     */
    @Pattern(regexp = "^(\\+\\d{1,3}[- ]?)?\\d{10}$",
            message = "Incorrect phone number format")
    private String phoneNumber;
    /**
     * Checks that the password is strong enough.
     * <p>
     * <b>Strong password criteria:</b>
     * <ul>
     * <li>Minimum 8 characters</li>
     * <li>Contains at least one uppercase letter</li>
     * <li>Contains at least one lowercase letter</li>
     * <li>Contains at least one number</li>
     * </ul>
     * </p>
     *
     * @return true if the password meets the criteria
     */
    //Not implemented yet. Saved for future
    public boolean isPasswordStrong() {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasUpper = !password.equals(password.toLowerCase());
        boolean hasLower = !password.equals(password.toUpperCase());
        boolean hasDigit = password.matches(".*\\d.*");
        return hasUpper && hasLower && hasDigit;
    }
    /**
     * Returns a string representation of the request (without the password).
     *
     * @return a string with the email and name
     */
    @Override
    public String toString() {
        return "RegisterRequest{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
