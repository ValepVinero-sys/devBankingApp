package com.Bank;

import com.Bank.Account;
import com.Bank.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for work with entity of {@link Transaction}.
 * <p>
 *     provides access for transaction history, supported pagination,
 *     search by account & filter by date's
 * </p>
 * @author Valep Vinreo
 * @version 1.0
 * @see Transaction
 * @see AccountRepository
 * @since 04-2026.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    /**
     *  Finds transaction pages where entered account are sender/received.
     *  <p>
     *      Result are filtered by creating date(from new to old)
     *  </p>
     * @param fromAccount sender's bill (cannot be null)
     * @param toAccount receiver bill (cannot be null)
     * @param pageable paginating param's(page number, size, sort)
     * @return page with transaction
     */
    //Not implemented yet. Saved for future
    Page<Transaction> findByFromAccountOrToAccountOrderByCreatedAtDesc(
            Account fromAccount, Account toAccount, Pageable pageable);

    /**
     *  Finds list of transaction where entered account are sander by set time period(from new to old) (TODO).
     *  <p>
     *      Useful for forming rapport's of expenses at certain period of time.
     *  </p>
     * @param account sender's bill
     * @param start Beginning of period
     * @param end end of period
     * @return List of transaction for certain period of time
     */
    List<Transaction> findByFromAccountAndCreatedAtBetween(
            Account account, LocalDateTime start, LocalDateTime end);

    /**
     *  Finds all transaction, connected to certain account.
     *  <p>
     *      Transaction are linked to account if account are sender or receiver.
     *  </p>
     *  <p>
     *      <B> SQL excample: </B>
     *      <pre>
     *      SELECT t FROM Transaction t
     *      WHERE t.fromAccount.accountNumber = :accountNumber OR
     *      t.toAccount.accountNumber = :accountNumber
     *      ORDER BY t.createdAt DESC
     *      </pre>
     *  </p>
     * @param accountNumber unique 20-digit's number
     * @return list of transaction sorted by creating date (from new to old).
     */
    @Query("SELECT t FROM Transaction t WHERE " +
            "t.fromAccount.accountNumber = :accountNumber OR " +
            "t.toAccount.accountNumber = :accountNumber ORDER BY t.createdAt DESC")
    List<Transaction> findTransactionsByAccountNumber(@Param("accountNumber") String accountNumber);
    /**
     * Finds transaction by unique ID (UUID).
     * <p>
     * Used for external system tracing of transaction status.
     * </p>
     *
     * @param transactionId unique UUID of transaction
     * @return transaction, if found.
     */
    Optional<Transaction> findByTransactionId(String transactionId);

    /**
     *  Count Transaction by certain status.
     *  <p>
     *      Can be used for monitoring (like failed Transaction's).
     *  </p>
     *
     * @param status status of Transaction (PENDING, COMPLETED, FAILED, CONCEALED).
     * @return number's of certain Transaction status.
     */
    long countByStatus(Transaction.TransactionStatus status);
    /**
     * Counts the number of outgoing transactions with a specific status.
     *
     * @param account : Sending account
     * @param status : Transaction status (PENDING, COMPLETED, FAILED)
     * @return number of transactions
     */
    long countByFromAccountAndStatus(Account account, Transaction.TransactionStatus status);

    /**
     * Counts the number of incoming transactions with a specific status.
     *
     * @param account : Receiving account
     * @param status : Transaction status
     * @return number of transactions
     */
    long countByToAccountAndStatus(Account account, Transaction.TransactionStatus status);
}
