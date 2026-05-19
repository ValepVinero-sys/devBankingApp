package com.Bank;
/**
 * DTO for responding to an error when executing a request.
 * <p>
 * A consistent error format for all API endpoints.
 * Used by the global exception handler {@link GlobalExceptionHandler}.
 * </p>
 *
 * <p>Example JSON response for a validation error:</p>
 * <pre>
 * {
 * "timestamp": "2025-01-15T14:25:00",
 * "status": 400,
 * "error": "Validation Failed",
 * "message": "Input data validation error",
 * "validationErrors": {
 * "email": "must be a well-formed email address",
 * "password": "size must be between 6 and 20"
 * },
 * "path": "/api/auth/register"
 * }
 * </pre>
 *
 * <p>Example JSON response for a business error:</p>
 * <pre>
 * {
 * "timestamp": "2025-01-15T14:30:00",
 * "status": 404,
 * "error": "Not Found",
 * "message": "Account number 1234567890 not found",
 * "path": "/api/accounts/1234567890"
 * }
 * </pre>
 *
 * @author Valep Vonery
 * @version 1.0
 * @see GlobalExceptionHandler
 * @since 04-2026
 */
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;
@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
        /**
         *Date & time of error appearing.
         * <p>formate: ISO 8601 {@code yyyy-MM-dd'T'HH:mm:ss}</p>
         */
        private LocalDateTime timestamp;
        /** Http error status code
         * <p>
         *     Most often error:
         *     <ul>
         *        <li>400 — Bad Request (validation error, not enough funds)</li>
         *        <li>401 — Unauthorized (not authorized)</li>
         *        <li>403 — Forbidden (no access)</li>
         *        <li>404 — Not Found (recourse not found)</li>
         *        <li>500 — Internal Server Error (unexcepted error)</li>
         *     </ul>
         * </p>
         *
         */
        private int status;
        /** Shorted error label (in English only).
         * <p>
         * matched basic http status's:
         * "Bad Request","Not Found","Internal Server Error" & etc.
         * </p>
         *
         */
        private String Error;
        /** Error message's for user's to see (language can be configured)
         * <p>
         *     Contains details for the user, such as:
         *     "Not enough funds on account. Available: 500.00 RUB"
         * </p>
         *
         */
        private String message;
        /** Details for validation error  (Only for status 400 Bad Request).
         * <p>
         *     Key - field name, purpose - error description.
         *     Example: {@code {"email": must be a valid email adress.}}
         * </p>
         *
         */
        private Map<String, String> validationErrors;
        /** Path (URI) for which the request was made.
         * <p>
         *     Helps quickly determine which endpoint caused the error.
         *     Example: {@code "/api/accounts/1234567890/transfer"}
         * </p>
         *
         */
        private String path;
    }

