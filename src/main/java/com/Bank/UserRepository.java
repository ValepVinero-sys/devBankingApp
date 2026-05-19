package com.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository for work with essence of {@link User}.
 * <p>
 *     represent method for search of user by email,
 *     and also for loading user with his account (JOIN FETCH).
 * </p>
 * @author Valep Vinreo
 * @version 1.0
 * @see AccountRepository
 * @see TransactionRepository
 * @since 04-2026
 */
    @Repository
    public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Finds user by email (unique field).
     * <p>
     *     Email use's like userName for spring boot.
     * </p>
     * @param Email Email address(register doesn't count)
     * @return User If found
     */
    Optional<User> findByEmail(String Email);

    /**
     * Check existence of user by entered email.
     *
     * @param Email Email address(register doesn't count).
     * @return {@code true} if User exist else {@code false}.
     */
        boolean existsByEmail(String Email);

    /**
     * Finds user by email & load his account
     * <p>
     *     Use's {@code LEFT JOINT FETCH} for  preventing N+1 request.
     *     In one request loading user & all his account.
     * </p>
     * <p>
     *     <b>Example of JPQL:</b>
     *     <pre>
     *     SELECT u FROM User u LEFT JOIN FETCH u.accounts WHERE u.email = :email
     *     </pre>
     *
     * </p>
     * @param Email Email address (register doesn't count)
     * @return User with initialized account list (or blank).
     */
    //Not implemented yet. Saved for future
        @Query("SELECT u FROM User u LEFT JOIN FETCH u.accounts WHERE u.email = :email")
        Optional<User> findByEmailWithAccounts(@Param("email") String Email);
    }

