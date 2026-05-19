package com.Bank;

import com.Bank.Account;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Entity for Bank User.
 *
 * <p>
 *      Represents a registered user of the banking application.
 *      Implements the {@link UserDetails} interface for integration with Spring Security.
 * </p>
 * <p><b>Key Features:</b></p>
 * <ul>
 *      <li>Email is used as a unique identifier and login.</li>
 *      <li>Passwords are stored encrypted (BCrypt).</li>
 *      <li>One user can have multiple bank accounts.</li>
 * </ul>
 * <p><b>Link with other entities:</b></p>
 * <ul>
 *      <li>{@link Account} — user accounts (one-to-many)</li>
 * </ul>
 *
 * <p><b>User roles:</b></p>
 * <ul>
 *      <li><b>ROLE_USER</b> — standard user (basic operations)</li>
 *      <li><b>ROLE_ADMIN</b> — administrator (additional privileges)</li>
 * </ul>
 * <p><b>Example of user registration:</b></p>
 * <pre>
 * User user = new User();
 * user.setEmail("user@example.com");
 * user.setPassword(passwordEncoder.encode("password123"));
 * user.setFirstName("John");
 * user.setLastName("Smith");
 * userRepository.save(user);
 * </pre>
 * @author Valep Vinreo
 * @version 1.0
 * @see Account
 * @see UserDetails
 * @since 04-2026
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
        /**
         * Unique identification of User.
         * <p>Primary key at Table {@code users}</p>
         */
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        /**
         * User's email address.
         * <p>
         * <b>Characteristics:</b>
         *  <ul>
         *   <li>Unique throughout the system</li>
         *   <li>Used as login</li>
         *   <li>Validated for correct format</li>
         *  </ul>
         * </p>
         */
        @Column(nullable = false, unique = true)
        private String email;

        /** Return user email.
         * @return user email
         */
        public String getEmail() {
                return email;
        }

        /**
         * Encrypted user password.
         * <p>
         * <b>Characteristics:</b>
         *  <ul>
         *   <li>Store encrypted (BCrypt algorithm)</li>
         *   <li>The original password is not stored</li>
         *   <li>Minimum password length: 6 characters (checked at the DTO level)</li>
         *  </ul>
         * </p>
         */
        @Column(nullable = false)
        private String password;
        /**
         * Name of user. Can be seen at dashboard & greetings.
         */
        private String firstName;
        /**
         * Last name of user.
         */
        private String lastName;
        /**
         * Date & time of user registration.
         * <p>Set automatically when user register.</p>
         */
        @Column(name = "created_at")
        private LocalDateTime createdAt = LocalDateTime.now();
        /**
         * List of user bank accounts.
         * <p>
         * <b>Relationship Type:</b> One-to-Many
         * </p>
         * <p>
         * <b>Loading:</b> LAZY — accounts are loaded only when explicitly requested.
         * To load accounts along with the user, use the
         * {@code userRepository.findByEmailWithAccounts()} method.
         * </p>
         *
         * @see Account
         * @see #getAccounts()
         */
        @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        private List<Account> accounts = new ArrayList<>();
        /**
         * Returns a list of user roles (access rights).
         * <p>
         * The current version supports the following roles:
         * <ul>
         * <li><b>ROLE_USER</b> — for all registered users</li>
         * <li><b>ROLE_ADMIN</b> — for administrators (requires additional configuration)</li>
         * </ul>
         * </p>
         *
         * @return a collection of {@link GrantedAuthority} access rights
         */
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }
        /**
         * Returns the user's email as the username for authentication.
         *
         * @return user's email
         */
        @Override
        public String getUsername() {
                return email;
        }
        /**
         * Checks if the account has expired.
        * <p>
        * In the current version, all accounts are considered valid.
        * </p>
        *
        * @return is always {@code true}
        */
        @Override
        public boolean isAccountNonExpired() { return true; }
        /**
         * Checks if the account is locked.
         * <p>
         * Account locking is not implemented in the current version.
         * </p>
         *
         * @return always {@code true}
         */
        @Override
        public boolean isAccountNonLocked() { return true; }
        /**
         * Checks if the password has expired.
         * <p>
         * In the current version, the password does not expire.
         * </p>
         *
         * @return always {@code true}
         */
        @Override
        public boolean isCredentialsNonExpired() { return true; }
        /**
         * Checks if the account is active.
         * <p>
         * Inactive users cannot log in.
         * In the current version, all accounts are considered active.
         * </p>
         *
         * @return always {@code true}
         */
        @Override
        public boolean isEnabled() { return true; }
        /**
         * Adds a new account to the user.
         * <p>
         * Important: simultaneously establishes the {@code account.setUser(this)} connection.
         * </p>
         *
         * @param account - the account to add
         */
        //Not implemented yet. Saved for future
        public void addAccount(Account account) {
                accounts.add(account);
                account.setUser(this);
        }
        /**
         * Deletes an account from a user.
         *
         * @param account - the account to delete
         */
        //Not implemented yet. Saved for future
        public void removeAccount(Account account) {
                accounts.remove(account);
                account.setUser(null);
        }
}

