package com.Bank;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
/**
 * DTO for the authentication (login) request.
 * <p>
 * Used in the {@code POST /api/auth/login} endpoint.
 * Contains the user's login credentials.
 * </p>
 * <p><b>Example of JSON-request:</b></p>
 * <pre>
 * {
 *   "email": "user@example.com",
 *   "password": "mySecretPassword123"
 * }
 * </pre>
 * <p><b>Field Validations:</b></p>
 * <ul>
 *   <li>email — обязательное поле, корректный email формат</li>
 *   <li>password — обязательное поле, минимум 6 символов</li>
 * </ul>
 *
 * <p><b>Example of validation error (400 Bad Request):</b></p>
 * <pre>
 * {
 *   "timestamp": "2025-01-15T10:30:00",
 *   "status": 400,
 *   "error": "Validation Failed",
 *   "validationErrors": {
 *     "email": "must be a well-formed email address",
 *     "password": "must not be blank"
 *   }
 * }
 * </pre>
 *
 * @author Valep Vinreo
 * @version 1.0
 * @see RegisterRequest
 * @since 04-2026
 */
@Data
public class LoginRequest {
    /**
     * User's email address.
     * <p>
     * <b>Requirements:</b>
     * <ul>
     * <li>Required field (cannot be null or empty)</li>
     * <li>Must match the email format (user@example.com)</li>
     * <li>Used as the login name</li>
     * <li>Case-sensitive validation</li>
     * </ul>
     * </p>
     *
     * <p><b>Examples of valid email addresses:</b></p>
     * <ul>
     * <li>user@example.com</li>
     * <li>ivan.ivanov@mail.ru</li>
     * <li>admin@company.org</li>
     * </ul>
     *
     * <p><b>Examples of invalid email addresses:</b></p>
     * <ul>
     * <li>user@ (domain missing)</li>
     * <li>user.example.com (missing @)</li>
     * <li>@example.com (missing username)</li>
     * </ul>
     */
    @NotBlank(message = "Email can't be blank")
    @Email(message = "Incorrect email format (Example: user@example.com)")
    private String email;
    /**
     * User password.
     * <p>
     * <b>Requirements:</b>
     * <ul>
     * <li>Required field (cannot be null or empty)</li>
     * <li>Minimum length: 6 characters</li>
     * <li>Maximum length: 100 characters</li>
     * <li>Transmitted in cleartext (HTTPS secures the connection)</li>
     * <li>Stored in the database encrypted (BCrypt)</li>
     * </ul>
     * </p>
     *
     * <p><b>Password recommendations:</b></p>
     * <ul>
     * <li>Use at least 8 characters</li>
     * <li>Mix letters (upper/lower case)</li>
     * <li>Add numbers and special characters</li>
     * <li>Do not use personal information (date of birth, name)</li>
     * </ul>
     *
     * <p><b>Examples of correct passwords:</b></p>
     * <ul>
     * <li>password123</li>
     * <li>MySecurePass!2025</li>
     * <li>qwerty12345</li>
     * </ul>
     *
     * @see org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
     */
    @NotBlank(message = "Password required")
    private String password;
}
