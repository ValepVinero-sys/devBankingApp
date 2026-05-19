package com.Bank;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utility class for working with JWT (JSON Web Tokens).
 * <p>
 * Responsible for:
 * <ul>
 * <li>generating JWT tokens after successful authentication</li>
 * <li>validating tokens (checking signature and expiration date)</li>
 * <li>extracting information from the token (user email)</li>
 * </ul>
 * </p>
 * <p><b>What is JWT?</b></p>
 * <p>
 * JWT (JSON Web Token) is a compact, URL-safe way to represent
 * claims between two parties. The token is digitally signed,
 * which guarantees its integrity and authenticity. * </p>
 * <p><b>JWT token structure:</b></p>
 * <pre>
 * xxxxx.yyyyy.zzzzz
 * | | |
 * Header Payload Signature
 * </pre>
 * <ul>
 * <li><b>Header:</b> contains the signature algorithm (HS256) and token type (JWT)</li>
 * <li><b>Payload:</b> contains claims (user information, creation time, expiration time)</li>
 * <li><b>Signature:</b> a signature created using a secret key</li>
 * </ul>
 * <p><b>JWT token lifecycle:</b></p>
 * <ol>
 * <li>User logs in → server generates token</li>
 * <li>Client stores token (localStorage or cookie)</li>
 * <li>Client adds token to each request header</li>
 * <li>Server verifies token on each request</li>
 * <li>After the time expires, the token becomes invalid.</li>
 * <li>The user must log in again to receive a new token.</li>
 * </ol>
 * <p><b>Example of a generated token:</b></p>
 * <pre>
 * eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.
 * eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNzA1MzA0MjAwLCJleHAiOjE3MDUzOTA2MDB9.
 * SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
 * </pre>
 * <p><b>Example of use in a controller:</b></p>
 * <pre>
 * {@code @PostMapping("/login")
 * public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest request) {
 * Authentication auth = authenticationManager.authenticate(...);
 * String jwt = jwtUtils.generateJwtToken(auth);
 * return ResponseEntity.ok(new JwtResponse(jwt));
 * }}</pre>
 * <p><b>⚠️ Important security notes:</b></p>
 * <ul>
 * <li>The secret key should NOT be stored in code — use {@code @Value} from application.yml</li>
 * <li>The key must be long enough (minimum 32 characters for HS256)</li>
 * <li>The token lifetime must be reasonable (24 hours in this project)</li>
 * <li>The token does not contain any sensitive information (only email)</li>
 * </ul>
 *
 * @author Valep Vinreo
 * @version 1.0
 * @see AuthTokenFilter
 * @see AuthController
 * @since 04-2026
 *
 */
@Component
@Slf4j
public class JwUtils {
    /**
     * Secret key for signing JWT tokens.
     * <p>
     * Stored in {@code application.yml} under the key {@code app.jwt.secret}.
     * </p>
     * <p>
     * <b>Secret key requirements:</b>
     * <ul>
     * <li>Length: minimum 32 characters</li>
     * <li>Complexity: mixed case letters, numbers, special characters</li>
     * <li>Uniqueness: different for different environments (dev, prod)</li>
     * <li>Confidentiality: should NOT be submitted to version control</li>
     * </ul>
     * </p>
     * <p>
     * <b>Example of a good key:</b>
     * {@code mySecretKeyForJWTGenerationWithAtLeast32CharactersLong!}
     * </p>
     *
     * @see #key()
     */
    @Value("${app.jwt.secret:mySecretKeyForJWTGenerationWithAtLeast32CharactersLong!}")
    private String jwtSecret;
    /**
     * JWT token lifetime in milliseconds.
     * <p>
     * Stored in {@code application.yml} under the {@code app.jwt.expiration} key.
     * </p>
     * <p>
     * <b>Default value:</b> 86400000 ms = 24 hours
     * </p>
     * <p>
     * <b>Recommendation:</b>
     * <ul>
     * <li>For development: 24 hours</li>
     * <li>For production: 1-8 hours (depending on security requirements)</li>
     * </ul>
     * </p>
     */
    @Value("${app.jwt.expiration:86400000}")
    private int jwtExpirationMs;
    /**
     * Creates an HMAC key for signing JWT tokens.
     * <p>
     * Uses the secret key from the configuration and converts it
     * into a {@link SecretKey} object for the HS256 algorithm.
     * </p>
     *
     * @return the secret key for signing and verifying JWTs
     *
     * @see Keys#hmacShaKeyFor(byte[])
     */
    private SecretKey key() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
    /**
     * Generates a JWT token based on successful authentication.
     * <p>
     * <b>How it works:</b>
     * <ol>
     * <li>Extracts the user's email from the {@link Authentication} object</li>
     * <li>Sets the email as the subject (sub) of the token</li>
     * <li>Sets the creation time (issuedAt, iat)</li>
     * <li>Sets the expiration time (expiration, exp) = current time + jwtExpirationMs</li>
     * <li>Signs the token using HMAC-SHA256</li>
     * <li>Returns a compact token string</li>
     * </ol>
     * </p>
     *
     * <p><b>Example of a generated token (in debug mode):</b></p>
     * <pre>
     * Token for user@example.com:
     * - Issued at: 2025-01-15T10:30:00
     * - Expires at: 2025-01-16T10:30:00
     * - Signature algorithm: HS256
     * </pre>
     *
     * @param authentication Spring Security authentication object (contains UserDetails)
     * @return JWT token string signed with the secret key
     *
     * @throws IllegalArgumentException if authentication does not contain UserDetails
     * @throws JwtException if an error occurred while creating the token
     *
     * @see Jwts#builder()
     * @see Jwts.SIG#HS256
     */
    public String generateJwtToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        String email = userPrincipal.getUsername();

        log.debug("Generating JWT token for user: {}", email);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        String token = Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key(), Jwts.SIG.HS256)
                .compact();

        log.debug("JWT token successfully generated for user: {}, expires: {}",
                email, expiryDate);

        return token;
    }
    /**
     * Extracts the user's email address from the JWT token.
     * <p>
     * This method does NOT verify the token's signature; it only decodes the payload.
     * For full validation, use {@link #validateJwtToken(String)}.
     * </p>
     *
     * <p><b>Workflow:</b></p>
     * <ol>
     * <li>Parses the token with signature verification</li>
     * <li>Extracts the "sub" claim (subject)</li>
     * <li>Returns the value as a string (user email)</li>
     * </ol>
     *
     * @param token JWT token to parse
     * @return the user email from the sub-field
     *
     * @throws JwtException if the token is malformed or the signature does not match
     * @throws IllegalArgumentException if the token is null or empty
     */
    public String getUserNameFromJwtToken(String token) {
        String email = Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();

        log.debug("Extracted email from JWT token: {}", email);
        return email;
    }

    /**
     * Checks the validity of a JWT token.
     * <p>
     * Performs the following checks:
     * <ul>
     * <li><b>Token format:</b> must contain 3 parts (header, payload, signature)</li>
     * <li><b>Signature:</b> must match the secret key</li>
     * <li><b>Expiration:</b> the token must not have expired</li>
     * </ul>
     * </p>
     *
     * <p><b>Error codes (for debugging):</b></p>
     * <ul>
     * <li>{@link io.jsonwebtoken.MalformedJwtException} - invalid token format</li>
     * <li>{@link SecurityException} - invalid signature</li>
     * <li>{@link io.jsonwebtoken.ExpiredJwtException} - the token has expired</li>
     * <li>{@link io.jsonwebtoken.UnsupportedJwtException} — unsupported format</li>
     * <li>{@link IllegalArgumentException} — empty token</li>
     * </ul>
     *
     * <p><b>Example of use in a filter:</b></p>
     * <pre>
     * if (jwtUtils.validateJwtToken(token)) {
     * String email = jwtUtils.getUserNameFromJwtToken(token);
     * UserDetails user = userDetailsService.loadUserByUsername(email);
     * // Set up authentication
     * }
     * </pre>
     *
     * @param authToken JWT token to verify
     * @return {@code true} if the token is valid (format, signature, expiration date),
     * {@code false} otherwise (error is logged)
     *
     * @see #logJwtError(String, String)
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(authToken);

            log.debug("JWT token verified successfully");
            return true;

        } catch (MalformedJwtException e) {
            logJwtError("Invalid JWT token format", e.getMessage());
        } catch (SecurityException e) {
            logJwtError("Invalid JWT token signature", e.getMessage());
        } catch (ExpiredJwtException e) {
            logJwtError("JWT token expired", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logJwtError("JWT token has an unsupported format", e.getMessage());
        } catch (IllegalArgumentException e) {
            logJwtError("JWT token is empty", e.getMessage());
        } catch (Exception e) {
            logJwtError("Unknown error validating JWT token", e.getMessage());
        }

        return false;
    }
    /**
     * Logs JWT token validation errors.
     * <p>
     * Moved to a separate method for consistent logging.
     * </p>
     *
     * @param errorType error type (human-readable description)
     * @param details error details (usually e.getMessage())
     */
    private void logJwtError(String errorType, String details) {
        log.warn("JWT validation error: {} - {}", errorType, details);
    }
    /**
     * Returns the token expiration time (for debugging).
     * <p>
     * Useful for diagnosing issues with expired tokens.
     * </p>
     *
     * @param token JWT token
     * @return token expiration date, or null if the token is invalid
     */
    public Date getExpirationDateFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();
        } catch (Exception e) {
            log.warn("Failed to get expiration date from token: {}", e.getMessage());
            return null;
        }
    }
    /**
     * Checks if the token has expired.
     *
     * @param token JWT token
     * @return {@code true} if the token has expired or cannot be parsed
     */
    public boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        if (expiration == null) {
            return true;
        }
        boolean expired = expiration.before(new Date());
        if (expired) {
            log.debug("The token has expired: {}", expiration);
        }
        return expired;
    }
    /**
     * Returns token information for debugging (safely, without displaying the full token).
     *
     * @param token JWT token
     * @return string representation with masked token
     */
    public String getTokenInfo(String token) {
        if (token == null || token.isEmpty()) {
            return "Token: null";
        }

        String maskedToken = token.length() > 20
                ? token.substring(0, 20) + "..."
                : token;

        String email = "unknown";
        try {
            email = getUserNameFromJwtToken(token);
        } catch (Exception e) {
            // ignore
        }

        return String.format("Token: %s, User: %s, Expired: %s",
                maskedToken, email, isTokenExpired(token));
    }
}
