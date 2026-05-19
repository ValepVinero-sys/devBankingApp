package com.Bank;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * Filtrate authentication of JWT token.
 * <p>
 *  Intercepts each HTTP request and checks the presence and validity of a JWT token
 *  in the {@code Authorization} header. If successful, sets the authentication
 *  in the {@link SecurityContextHolder} for the current request.
 * </p>
 * <p><b>Place in the Spring Security filter chain:</b></p>
 * <pre>
 * Request → AuthTokenFilter → UsernamePasswordAuthenticationFilter → ... → Controller
 *                ↓
 *           Check JWT token
 *                ↓
 *           Setup authentication
 * </pre>
 * <p><b>Request processing thread:</b></p>
 * <ol>
 *     <li>Extracts the JWT token from the {@code Authorization: Bearer &lt;token&gt;} header</li>
 *     <li>Verifies the signature and expiration date of the token</li>
 *     <li>Extracts the user's email from the token</li>
 *     <li>Loads user data from the database</li>
 *     <li>Creates an {@link org.springframework.security.core.Authentication} object and saves it to the context</li>
 *     <li>Passes the request down the chain</li>
 * </ol>
 * <p><b>Example of a valid header:</b></p>
 * <pre>
 * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.
 * eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNTE2MjM5MDIyfQ.
 * SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
 * </pre>
 * <p><b>Speciality:</b></p>
 * <ol>
 *   <li>This filter inherits from {@link OncePerRequestFilter} — ensures execution per request</li>
 *   <li>Public endpoints (registration, login) are skipped without authentication</li>
 *   <li>If the token is invalid, the request continues, but authentication is not established</li>
 *   <li>Validation errors are logged but do not interrupt request processing</li>
 * </ol>
 * @author Valep Vinreo
 * @version 1.0
 * @see JwUtils
 * @see UserDetailsServiceImpl
 * @since 04-2026
 *
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthTokenFilter extends OncePerRequestFilter {

    private final JwUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    /**
     * The main filtering method called for each HTTP request.
     * <p>
     * <b>Workflow:</b>
     * <ol>
     * <li><b>Check public paths</b> — if the path is public,
     * the request is immediately passed on</li>
     * <li><b>Extract token</b> — parses the {@code Authorization} header</li>
     * <li><b>Validate token</b> — checks the signature, expiration date, and format</li>
     * <li><b>Extract email</b> — gets the user's email from the token</li>
     * <li><b>Load user</b> — gets data from the database</li>
     * <li><b>Create authentication</b> — creates an {@link org.springframework.security.core.Authentication} object</li>
     * <li><b>Set to context</b> — saves authentication for the current request</li>
     * <li><b>Pass request</b> — continues processing the request</li>
     * </ol>
     * </p>
     *
     * <p><b>Important points:</b></p>
     * <ul>
     * <li>An invalid token does NOT abort the request — authentication simply isn't established</li>
     * <li>If a validation error occurs, the request is processed as unauthenticated</li>
     * <li>All errors are logged for debugging and auditing</li>
     * </ul>
     *
     *
     * @param request HTTP request
     * @param response HTTP response
     * @param filterChain filter chain
     * @throws ServletException if a servlet error occurred
     * @throws IOException if an I/O error occurred
     */

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();
        boolean isPublicPath = path.equals("/") || path.equals("/login") || path.equals("/register") ||
                path.equals("/dashboard") || path.equals("/accounts") || path.equals("/transfer") || path.equals("/transactions") ||
                path.startsWith("/css") || path.startsWith("/js") || path.startsWith("/api/auth");

        if (isPublicPath) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = parseJwt(request);
            log.debug("JWT token: {}", jwt != null ? "present" : "missing");

            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                log.debug("Authenticated user: {}", username);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                log.debug("No valid JWT token found for path: {}", path);
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the JWT token from the {@code Authorization} HTTP header.
     * <p>
     * <b>Expected header format:</b>
     * <pre>
     * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     * </pre>
     * </p>
     *
     * <p><b>Extraction algorithm:</b></p>
     * <ol>
     * <li>Gets the value of the {@code Authorization} header</li>
     * <li>Checks that the header is not empty</li>
     * <li>Checks that the header starts with the {@code Bearer } prefix</li>
     * <li>Returns the part after the prefix (the token itself)</li>
     * </ol>
     * </p>
     * <p><b>Examples:</b></p>
     * <pre>
     * Example 1 (valid):
     * Header: "Bearer" eyJhbGciOiJIUzI1NiIs..."
     * Result: "eyJhbGciOiJIUzI1NiIs..."
     *
     * Example 2 (invalid, no prefix):
     * Header: "eyJhbGciOiJIUzI1NiIs..."
     * Result: null
     *
     * Example 3 (empty header):
     * Header: null
     * Result: null
     * </pre>
     *
     * @param request HTTP request
     * @return JWT token (without the "Bearer" prefix), or null if the header is missing
     * or has an invalid format
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        log.debug("Authorization header: {}", headerAuth != null ? "present" : "missing");

        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            String token = headerAuth.substring(7);
            log.debug("JWT token extracted (length: {} chars)", token.length());
            return token;
        }
        return null;
    }
    /**
     * Clears authentication from the SecurityContext (e.g., on logout).
     */
    public void clearAuthentication() {
        SecurityContextHolder.clearContext();
        log.debug("Authentication cleared");
    }
}
