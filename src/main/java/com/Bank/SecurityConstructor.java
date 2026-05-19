package com.Bank;
/**
 * Spring Security security configuration.
 *
 * Settings:
 * - JWT authentication (stateless)
 * - BCrypt for password encryption
 * - CSRF disabled
 * - Public endpoints: /, /login, /register, /api/auth/**
 * - Protected endpoints: require a JWT token
 * - Admin endpoints: require the ADMIN role
 *
 *  @author Valep Vinreo
 * @version 1.0
 * @since 04-2026
 */
import com.Bank.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConstructor {

    private final AuthTokenFilter authTokenFilter;

    /**
     * Creates a bean for encrypting passwords using the BCrypt algorithm. * <p>
     * <b>BCrypt Features:</b>
     * <ul>
     * <li>Adaptive hash algorithm (slows down as computers become more powerful)</li>
     * <li>Automatically adds salt to protect against rainbow tables</li>
     * <li>Output length: 60 characters</li>
     * <li>Format: {@code $2a$10$...}</li>
     * </ul>
     * </p>
     *
     * <p><b>Usage examples:</b></p>
     * <pre>
     * // Hashing a password during registration
     * String encodedPassword = passwordEncoder.encode("userPassword123");
     *
     * // Validating a password during login
     * boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
     * </pre>
     *
     * <p><b>Important:</b>
     * <ul>
     * <li>The original password is NOT saved—only a hash</li>
     * <li>Even an administrator cannot find out the user's password</li>
     * <li>Two identical phrases will produce different hashes (due to the salt)</li>
     * </ul>
     * </p>
     * @return {@link PasswordEncoder} bean for encrypting passwords
     *
     * @see BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Creates an {@link AuthenticationManager} bean—the core authentication component of Spring Security.
     * <p>
     * Used in {@link com.Bank.AuthController} to authenticate the user
     * upon login. * </p>
     *
     * <p><b>Workflow:</b></p>
     * <ol>
     * <li>Accepts email and password in the form {@link org.springframework.security.authentication.UsernamePasswordAuthenticationToken}</li>
     * <li>Delegates validation to {@link UserDetailsServiceImpl#loadUserByUsername(String)}</li>
     * <li>Verifies the password via {@link PasswordEncoder}</li>
     * <li>Returns a populated {@link org.springframework.security.core.Authentication} object or throws an exception</li>
     * </ol>
     *
     * <p><b>Example of use in AuthController:</b></p>
     * <pre>
     * Authentication auth = authenticationManager.authenticate(
     * new UsernamePasswordAuthenticationToken(email, password)
     * );
     * </pre>
     *
     * @param authConfig Spring Security authentication configuration
     * @return bean {@link AuthenticationManager}
     * @throws Exception if the authentication manager could not be created
     *
     * @see AuthenticationConfiguration
     * @see AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Configures the main Spring Security security filter chain. * <p>
     * <b>Security Settings:</b>
     * <ul>
     * <li><b>CSRF:</b> Disabled — CSRF attacks are not relevant when using JWT tokens.</li>
     * <li><b>Sessions:</b> Stateless — the server does not store session state.</li>
     * <li><b>Authorization:</b> Endpoint access rules.</li>
     * </ul>
     * </p>
     *
     * <p><b>Access rules (from most specific to general):</b></p>
     * <pre>
     * permitAll() → public pages (/, /login, /register)
     * permitAll() → Swagger UI (/swagger-ui/**, /v3/api-docs/**)
     * permitAll() → Authentication API (/api/auth/**)
     * authenticated() → all others Endpoints (require JWT)
     * </pre>
     *
     * <p><b>Filter chain:</b></p>
     * <pre>
     * Request → AuthTokenFilter (JWT check) → Spring Security Filters → Controller
     * </pre>
     *
     * @param http {@link HttpSecurity} object for configuration
     * @return the assembled filter chain {@link SecurityFilterChain}
     * @see HttpSecurity
     * @see SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/register", "/dashboard",
                                "/accounts", "/transfer", "/transactions",
                                "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
