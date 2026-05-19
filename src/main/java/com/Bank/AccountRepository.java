package com.Bank;

import com.Bank.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for work with essences{@link Account}.
 * <p>
 *     Contain method for access to Bank account at DataBase.
 *     Except basic crud method's, contain custom request
 *     for search by account number, receiving user account & execution pessimistic locking.
 * </p>
 * @author Valep Vinreo
 * @version 1.00
 * @see Account
 * @see UserRepository
 * @since 04-2026
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /** Finds Bank accounts by unique number.
     * <p>
     *     Method use's naming agreement's of Spring Data JPA:
     * {@code findByAccountNumber} generate request
     * {@code SELECT a FROM Account a WHERE a.accountNumber = ?1}.
     * </p>
     *
     * @param accountNumber account number (20-digit's unique number's).
     * @return {@link Optional}, containing found account, or blank if account doesn't exist.
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     *  Finds all account's belonged to user.
     *
     * @param userId user identification (primary key from user tabs).
     * @return list of found user account's (can be blank).
     */
    //Not implemented yet. Saved for future
    List<Account> findByUserId(Long userId);

    /**
     * find all account's belonged to user (by object user).
     * <p>
     *     Alt {@link #findByUserId(Long)} in case user object already loaded.
     * </p>
     * @param user object user(cannot be null).
     * @return list of user account's
     */
    List<Account> findByUser(User user);

    /**
     *  Checking existence of account by entered number.
     *
     * @param accountNumber (20-digit's unique number's).
     * @return {@code true}  if account exist else {@code false}.
     */
    boolean existsByAccountNumber(String accountNumber);

    /**
     *  Finds account by number with pessimistic lock on Data Base level.
     *  <p>
     *      Use's {@link LockModeType#PESSIMISTIC_WRITE} for preventing same time balance change's
     *      during parallel transaction. Lock auto-lifted after the transaction ended.
     *  </p>
     *  <p>
     *      <b>Important: </b> this method should call changes inside transaction's
     *      annotated {@code @Transactional}. Else lock won't be applied.
     *  </p>
     * @param accountNumber unique 20-digit's number
     * @return {@link Optional} with found account's locked for write.
     * @see LockModeType#PESSIMISTIC_WRITE
     * @see org.springframework.transaction.annotation.Transactional
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber")
    Optional<Account> findForUpdate(@Param("accountNumber") String accountNumber);
}
