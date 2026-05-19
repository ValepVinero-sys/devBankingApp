package com.Bank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for the response upon successful user authentication.
 * <p>
 * Returned after a successful login via the
 * {@code POST /api/auth/login} endpoint. Contains the JWT token that the client
 * should use for authorization in subsequent requests. * </p>
 *
 * <p><b>Token lifecycle:</b></p>
 * <ol>
 * <li>The client sends email and password to {@code /api/auth/login}</li>
 * <li>The server verifies the credentials</li>
 * <li>If successful, generates a JWT token and returns it in {@link JwtResponse}</li>
 * <li>The client stores the token (usually in {@code localStorage} or {@code sessionStorage})</li>
 * <li>The client adds the token to the {@code Authorization} header on each request</li>
 * <li>The token is valid for 24 hours, after which reauthentication is required</li>
 * </ol>
 *
 * <p><b>Sample response (200 OK):</b></p>
 * <pre>
 * {
 * "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNzA1MzA0MjAwLCJleHAiOjE3MDUzOTA2MDB9.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
 * "type": "Bearer"
 * }
 * </pre>
 *
 * <p><b>Client Instructions (JavaScript):</b></p>
 * <pre>
 * // 1. Save the token after login
 * localStorage.setItem('token', response. Token);
 *
 * // 2. Add the token to the headers of all requests
 * fetch('/api/accounts', {
 * headers: {
 * 'Authorization': 'Bearer' + localStorage.getItem('token')
 * }
 * });
 * </pre>
 *
 * <p><b>Instructions for the client (Thymeleaf + JavaScript):</b></p>
 * <pre>
 * // Login form in login.html
 * fetch('/api/auth/login', {
 * method: 'POST',
 * headers: { 'Content-Type': 'application/json' },
 * body: JSON.stringify({ email, password })
 * })
 * .then(response => response.json())
 * .then(data => {
 * localStorage.setItem('jwtToken', data.token);
 * localStorage.setItem('tokenType', data.type);
 * window.location.href = '/dashboard';
 * });
 * </pre>
 *
 * <p><b>⚠️ Important notes:</b></p>
 * <ul>
 * <li>The token should NOT be stored in {@code localStorage} if security is a concern
 * (it is better to use {@code httpOnly cookies})</li>
 * <li>This project uses {@code localStorage} for simplicity</li>
 * <li>The token does not contain any sensitive user data</li>
 * <li>The token expires after 24 hours (configured in {@code application.yml})</li>
 * </ul>
 *
 * @author Valep Vinreo
 * @version 1.0
 * @see JwUtils
 * @see AuthController#loginUser(LoginRequest)
 * @since 04-2026
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse {

    /**
     * JWT access token.
     * <p>
     * <b>Token structure:</b>
     * <pre>
     * xxxxx.yyyyy.zzzzz
     * | | |
     * Header Payload Signature
     * </pre>
     * </p>
     *
     * <p><b>What's in the token:</b></p>
     * <ul>
     * <li><b>Header:</b> Signature algorithm (HS256)</li>
     * <li><b>Payload (claims):</b>
     * <ul>
     * <li>sub — user email</li>
     * <li>iat — issue date (timestamp)</li>
     * <li>exp — expiration date (timestamp)</li>
     * </ul>
     * </li>
     * <li><b>Signature:</b> Signature confirming the integrity of the token</li>
     * </ul>
     * </p>
     *
     * <p><b>Example of use in the client:</b></p>
     * <pre>
     * const token = response.token;
     * localStorage.setItem('authToken', token);
     * </pre>
     */
    private String token;

    /**
     * Token type for the {@code Authorization} header.
     * <p>
     * Always {@code "Bearer"}, as per the JWT standard.
     * The client must use this exact value in the header:
     * </p>
     * <pre>
     * Authorization: Bearer &lt;token&gt;
     * </pre>
     *
     * <p><b>JavaScript example:</b></p>
     * <pre>
     * fetch('/api/accounts', {
     * headers: {
     * 'Authorization': `${response.type} ${response.token}`
     * }
     * });
     * </pre>
     */
    private String type = "Bearer";

    /**
     * Creates a response with only the token (the type is set automatically).
     *
     * @param token JWT token
     */
    public JwtResponse(String token) {
        this.token = token;
        this.type = "Bearer";
    }

    /**
     * Returns the full authorization header for the client's convenience.
     * <p>
     * Example: {@code "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."}
     * </p>
     *
     * @readyonly This is a computed field (not serialized to JSON)
     * @return a string for the Authorization header
     */
    public String getAuthorizationHeader() {
        return type + " " + token;
    }

/**
 * Checks if the token has expired (approximately).
 * <p>
 * <b>Note:</b> Full validation is performed on the server.
 * This method is for client-side debugging only.
 * </p>
 *
 * @param currentTime current time in milliseconds
 * @return true if the token has expired or cannot be decoded
 * */
    //Not implemented yet. Saved for future
    public boolean isExpired(long currentTime) {
     try {
    // Decode the payload (without signature verification)
        String[] parts = token.split("\\.");
        if (parts.length != 3) return true;

        String payloadJson = new String(java.util.Base64.getDecoder().decode(parts[1]));
    // Simplified parsing for the example
        return payloadJson.contains("\"exp\"");
    } catch (Exception e) {
        return true;
    }
}
    /**
     * Secure string representation (hides part of the token).
     *
     * @return a string displaying only the first 20 characters of the token
     */
    @Override
    public String toString() {
        String maskedToken = token != null && token.length() > 20
                ? token.substring(0, 20) + "..."
                : "[empty]";
        return "JwtResponse{" +
                "token='" + maskedToken + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
