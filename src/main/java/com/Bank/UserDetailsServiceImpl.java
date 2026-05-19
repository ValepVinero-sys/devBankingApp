package com.Bank;

import com.Bank.User;
import com.Bank.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementation of the user data loading service for Spring Security.
 * <p>
 * This service integrates our database with the Spring Security authentication mechanism. * When a user logs in, Spring Security calls {@link #loadUserByUsername(String)}
 * to retrieve user data via email.
 * </p>
 *
 * <p><b>Place in the Spring Security architecture:</b></p>
 * <pre>
 * Login Request (email, password)
 * ↓
 * AuthenticationManager
 * ↓
 * UserDetailsService ←─────────┐
 * ↓ │
 * UserDetails (our User class) │
 * ↓ │
 * PasswordEncoder checks the password
 * ↓ │
 * Authentication success/failure ──┘
 * </pre>
 * <p><b>Authentication lifecycle:</b></p>
 * <ol>
 * <li>The user sends an email and password to {@code /api/auth/login}</li>
 * <li>Spring Security calls {@code loadUserByUsername(email)}</li>
 * <li>The method finds the user in the database by email</li>
 * <li>If the user is not found, an {@link UsernameNotFoundException} is thrown</li>
 * <li>Spring Security compares the password from the request with the hash from the database (via {@code PasswordEncoder})</li>
 * <li>If they match, authentication is successful and a JWT token is generated</li>
 * </ol>
 *
 * <p><b>Example of use (implicitly, via Spring Security):</b></p>
 * <pre>
 * // In AuthController:
 * Authentication authentication = authenticationManager.authenticate(
 * new UsernamePasswordAuthenticationToken(email, password)
 * );
 * // Spring Security automatically calls UserDetailsServiceImpl.loadUserByUsername(email)
 * </pre>
 *
 * <p><b>⚠️ Important notes:</b></p>
 * <ul>
 * <li>Email is used as the username (login)</li>
 * <li>The password in the database is stored encrypted (BCrypt)</li>
 * <li>Password comparison is performed by Spring Security via {@code PasswordEncoder}</li>
 * <li>The method must always return {@link UserDetails}, not null</li>
 * </ul>
 *
 * @author Valep Vinreo
 * @version 1.0
 * @see UserDetailsService
 * @see UserDetails
 * @see User
 * @see SecurityConstructor
 * @since 04-2026
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads user data by email for Spring Security authentication.
     * <p>
     * <b>How it works:</b>
     * <ol>
     * <li>Accepts an email (login) from Spring Security</li>
     * <li>Looks up the user in the database using {@link UserRepository#findByEmail(String)}</li>
     * <li>If the user is found, it returns an {@link User} object,
     * which implements the {@link UserDetails} interface</li>
     * <li>If the user is not found, it throws an {@link UsernameNotFoundException}</li>
     * </ol>
     * </p>
     *
     * <p><b>What happens after UserDetails is returned:</b></p>
     * <ul>
     * <li>Spring Security extracts the password from the UserDetails object</li>
     * <li>Compares it with the password from the request using {@code PasswordEncoder}</li>
     * <li>Also checks the status account (isEnabled, isAccountNonLocked, etc.)</li>
     * </ul>
     *
     * <p><b>Example of a database query:</b></p>
     * <pre>
     * SELECT * FROM users WHERE email = ?
     * </pre>
     *
     * @param email - user's email (used as login)
     * @return {@link User} object (implements {@link UserDetails})
     *
     * @throws UsernameNotFoundException if the user with the specified email address is not found in the database
     *
     * @see UserRepository#findByEmail(String)
     * @see User#getEmail()
     * @see User#getPassword()
     * @see User#getAuthorities()
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Attempt to load user by email: {}", email);

// Search for a user in the database
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User with email {} not found in the database", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        log.debug("User successfully loaded: {}, roles: {}",
                user.getEmail(), user.getAuthorities());

        return user;
    }
}
