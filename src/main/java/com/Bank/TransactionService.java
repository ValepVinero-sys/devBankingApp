package com.Bank;

import com.Bank.TransactionResponse;
import com.Bank.Transaction;
import com.Bank.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 *  A service for managing bank transaction history.
 * <p>
 * Provides methods for retrieving, filtering, and searching transactions.
 * Used to display transaction history in your personal account,
 * generate statements, and perform audits.
 * </p>
 * <p><b>Main function:</b></p>
 * <ul>
 *     <li>Receive all user (with pagination) transaction;</li>
 *     <li>Search among transaction by account number;</li>
 *     <li>Filtration transaction (by creation date);</li>
 *     <li>receive statistic of transaction.</li>
 * </ul>
 * <p><b>Productivity: </b></p>
 * <ul>
 *     <li>All Read-Only method optimized for reading;</li>
 *     <li>Using pagination for big amount of data;</li>
 *     <li>Index at DB by fields from_account_id, to_account_id, created_at</li>
 * </ul>
 *
 * @author Valep Vonery
 * @version 1.0
 * @see Transaction
 * @see TransactionRepository
 * @see AccountService
 * @since 04-2026
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    /**
     * Returns a page of all user transactions with pagination.
     * <p>
     * <b>Working algorithm:</b>
     * <ol>
     * <li>Gets all user accounts</li>
     * <li>For each account, finds transactions (both incoming and outgoing)</li>
     * <li>Combine the results and sort by date</li>
     * <li>Returns a paginated page</li>
     * </ol>
     * </p>
     *
     * <p><b>Example of use:</b></p>
     * <pre>
     *     //First page with 10 records sorted by date(from new to old).
     * Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
     * Page<TransactionResponse> transactions = transactionService.getUserTransactions(user, pageable);
     * </pre>
     *
     * @param user     User which transaction we looking-to
     * @param pageable param of pagination (page number, size, sorting)
     * @return pages with {@link TransactionResponse}
     * @throws IllegalArgumentException if user == null
     */
    //Not implemented yet. Saved for future
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getUserTransactions(com.Bank.User user, Pageable pageable) {
        log.debug("Transaction request for user: {}, page: {}, size: {}",
        user.getEmail(), pageable.getPageNumber(), pageable.getPageSize());
        // Receiving all user accounts
        List<Account> userAccounts = accountRepository.findByUser(user);
        List<Long> accountIds = userAccounts.stream()
                .map(Account::getId)
                .collect(Collectors.toList());

        // TODO: Implement a method in the repository to search the list of accounts
        // return transactionRepository.findByFromAccountIdInOrToAccountIdIn(accountIds, accountIds, pageable)
        //         .map(TransactionResponse::fromEntity);

        return Page.empty();
    }

    /**
     * Finds all transactions associated with the specified account number.
     * <p>
     * A transaction is considered associated with an account if:
     * <ul>
     * <li>the account is the sender (fromAccount), or</li>
     * <li>the account is the recipient (toAccount)</li>
     * </ul>
     * </p>
     *
     * <p><b>SQL Example (HQL):</b></p>
     * <pre>
     * SELECT t FROM Transaction t
     * WHERE t.fromAccount.accountNumber = :accountNumber
     * OR t.toAccount.accountNumber = :accountNumber
     * ORDER BY t.createdAt DESC
     * </pre>
     *
     * @param accountNumber account number (20 digits, unique)
     * @return list of transactions sorted by date (newest on top)
     *
     * @throws ResourceNotFoundException if The account with the specified number does not exist.
     * @throws SecurityException if the user does not have access to this account.
     *
     * @see #getTransactionsByAccountNumberWithPagination(String, int, int)
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByAccountNumber(String accountNumber) {
        log.debug("Searching transaction of account: {}", accountNumber);

        // Checking existence of account
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));
        List<Transaction> transactions = transactionRepository.findTransactionsByAccountNumber(accountNumber);
        log.debug("Found transaction for account {}: {}", accountNumber, transactions.size());
        return transactions.stream()
                .map(TransactionResponse::fromEntity)
                .collect(Collectors.toList());
    }
    /**
     * Finds transactions by account number with pagination.
     * <p>
     * Recommended for large data volumes instead of
     * {@link #getTransactionsByAccountNumber(String)}.
     * </p>
     *
     * @param accountNumber account number
     * @param page page number (starting from 0)
     * @param size number of records per page
     * @return page with transactions
     *
     * @see #getTransactionsByAccountNumber(String)
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionsByAccountNumberWithPagination(
            String accountNumber, int page, int size) {

        log.debug("Searching for transactions for account {} with pagination: page={}, size={}",
                accountNumber, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // TODO: realize method in repository
        // return transactionRepository.findByFromAccountAccountNumberOrToAccountAccountNumber(
        //         accountNumber, accountNumber, pageable)
        //         .map(TransactionResponse::fromEntity);

        return Page.empty();
    }
    /**
     * Finds all transactions made from the specified account for a specified period.
     *
     * <p>
     * Useful for:
     * <ul>
     * <li>Generating a monthly expense report</li>
     * <li>Analyzing expenses for a specific period</li>
     * <li>Downloading statements for a specified interval</li>
     * </ul>
     * </p>
     *
     * <p><b>Use example:</b></p>
     * <pre>
     * // Transactions for the last 30 days
     * LocalDateTime end = LocalDateTime.now();
     * LocalDateTime start = end.minusDays(30);
     * List<TransactionResponse> transactions = transactionService
     * .getTransactionsByDateRange(account, start, end);
     * </pre>
     *
     * @param account - the account from which the transactions were sent
     * @param startDate - the start of the period (inclusive)
     * @param endDate - the end of the period (inclusive)
     * @return - a list of transactions sorted by date (oldest first)
     *
     * @throws IllegalArgumentException if startDate > endDate
     */
    //Not implemented yet. Saved for future
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByDateRange(
            Account account, LocalDateTime startDate, LocalDateTime endDate) {

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be later than end date");
        }

        log.debug("Searching for transactions for account {} for the period: {} - {}",
                account.getAccountNumber(), startDate, endDate);

        List<Transaction> transactions = transactionRepository
                .findByFromAccountAndCreatedAtBetween(account, startDate, endDate);

        log.debug("Found transaction: {}", transactions.size());
        return transactions.stream()
                .map(TransactionResponse::fromEntity)
                .collect(Collectors.toList());
    }
    /**
     * Finds a transaction by its internal ID.
     * <p>
     * Used to retrieve detailed information about a specific transfer.
     * </p>
     *
     * @param transactionId internal transaction ID (primary key in the database)
     * @return {@link TransactionResponse} with transaction details
     *
     * @throws ResourceNotFoundException if a transaction with the specified ID is not found
     */
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long transactionId) {
        log.debug("Search transaction by ID: {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transactionId));

        return TransactionResponse.fromEntity(transaction);
    }
    /**
     * Finds a transaction by its external UUID.
     * <p>
     * Used by external systems to track transfer status.
     * </p>
     *
     * @param uuid external transaction identifier (UUID)
     * @return {@link TransactionResponse} with transaction details
     *
     * @throws ResourceNotFoundException if a transaction with the specified UUID is not found
     */
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionByUuid(String uuid) {
        log.debug("Search for transaction by UUID: {}", uuid);

        Transaction transaction = transactionRepository.findByTransactionId(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + uuid));

        return TransactionResponse.fromEntity(transaction);
    }
    /**
     * Gets the total amount of all transfers (incoming and outgoing) for an account.
     * @param accountNumber account number
     * @return an array of two values: [outgoing amount, incoming amount]
     */
    //Not implemented yet. Saved for future
    @Transactional(readOnly = true)
    public BigDecimal[] getTransactionSummary(String accountNumber) {
        log.debug("Calculating statistics for account: {}", accountNumber);
        List<Transaction> transactions = transactionRepository.findTransactionsByAccountNumber(accountNumber);

        BigDecimal outgoingTotal = BigDecimal.ZERO;
        BigDecimal incomingTotal = BigDecimal.ZERO;

        for (Transaction transaction : transactions) {
            if (transaction.getFromAccount() != null &&
                    transaction.getFromAccount().getAccountNumber().equals(accountNumber)) {
                outgoingTotal = outgoingTotal.add(transaction.getAmount());
            }
            if (transaction.getToAccount() != null &&
                    transaction.getToAccount().getAccountNumber().equals(accountNumber)) {
                incomingTotal = incomingTotal.add(transaction.getAmount());
            }
        }

        log.debug("Statistics for account {}: outgoing={}, incoming={}",
                accountNumber, outgoingTotal, incomingTotal);

        return new BigDecimal[]{outgoingTotal, incomingTotal};
    }
    /**
     * Counts the number of transactions with a specific status.
     * <p>
     * Used to monitor system health:
     * <ul>
     * <li>Number of failed transactions (FAILED)</li>
     * <li>Number of pending transactions (PENDING)</li>
     * </ul>
     * </p>
     *
     * @param status transaction status (PENDING, COMPLETED, FAILED, CANCELLED)
     * @return number of transactions with the specified status
     */
    //Not implemented yet. Saved for future
    @Transactional(readOnly = true)
    public long countByStatus(Transaction.TransactionStatus status) {
        log.debug("Counting transactions with status: {}", status);
        return transactionRepository.countByStatus(status);
    }

}
