package com.Bank;

import com.Bank.AccountResponse;
import com.Bank.ResourceNotFoundException;
import com.Bank.Account;
import com.Bank.User;
import com.Bank.AccountRepository;
import com.Bank.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
/**
 * Bank account management service.
 * <p>
 * Provides business logic for account operations:
 * <ul>
 * <li>Creating new accounts</li>
 * <li>Searching for accounts by various criteria</li>
 * <li>Generating unique account numbers</li>
 * </ul>
 * </p>
 *
 * <p>All methods that modify data are annotated with {@code @Transactional}
 * to ensure database integrity and automatic error rollback.</p>
 *
 * @author Valep Vonery
 * @version 1.0
 * @see Account
 * @see AccountRepository
 * @see TransferService
 * @since 04-2026
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    /**
     * Create new Bank account for current authenticated user.
     * <p>
     * <b>Algorithm:</b>
     * <ol>
     *   <li>Extract email of user from context of Spring Security</li>
     *   <li>Find user in DB by email</li>
     *   <li>generate unique 20-digits account number</li>
     *   <li>Create new object {@link Account} & fill his data</li>
     *   <li>save account to DB</li>
     *   <li>Transform entity to DTO & return result</li>
     * </ol>
     * </p>
     *
     * <p><b>Example of use:</b></p>
     * <pre>
     * CreateAccountRequest request = new CreateAccountRequest();
     * request.setType(AccountType.SAVINGS);
     * request.setCurrency("USD");
     * AccountResponse response = accountService.createAccount(request);
     * System.out.println("Номер счёта: " + response.getAccountNumber());
     * </pre>
     *
     * @param request object with param of new account (type, currency)
     * @return {@link AccountResponse} with data of created account.
     *
     * @throws ResourceNotFoundException if user with current email not found at DB
     * @throws IllegalArgumentException if transferred incorrect type of account or currency
     *
     * @see CreateAccountRequest
     * @see AccountResponse
     * @see SecurityContextHolder
     */
    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Account account = new Account();
        account.setAccountNumber(generateAccountNumber());
        account.setUser(user);
        account.setType(request.getType());
        account.setCurrency(request.getCurrency());
        account.setBalance(java.math.BigDecimal.ZERO);

        Account savedAccount = accountRepository.save(account);
        return AccountResponse.fromEntity(savedAccount);
    }
    /**
     * Return list of all accounts linked to current authenticated user.
     * <p>
     * Method use Spring Security for definitions current user
     * & return all his accounts, sorted by creation date (from new to old).
     * </p>
     *
     * <p><b>Possible variate of use:</b></p>
     * <ul>
     *   <li>Show list of accounts at dashboard</li>
     *   <li>Choose of account for doing transfer</li>
     *   <li>Calculate all funds of user</li>
     * </ul>
     *
     * @return List {@link AccountResponse}, returns an empty list if the user has no accounts
     *
     * @throws ResourceNotFoundException if user with entered email not founds
     * @throws org.springframework.security.core.AuthenticationException if user not authenticated
     *
     * @see AccountResponse
     * @see SecurityContextHolder
     */
    @Transactional(readOnly = true)
    public List<AccountResponse> getUserAccounts() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return accountRepository.findByUser(user).stream()
                .map(AccountResponse::fromEntity)
                .collect(Collectors.toList());
    }
    /**
     * Found Bank account by his unique number.
     * <p>
     * <b>Important safety check:</b>
     * <ul>
     *   <li>check existence if account in DB</li>
     *   <li>Check if current user have access to account(master or admin)</li>
     * </ul>
     * </p>
     *
     * <p><b>Access right:</b></p>
     * <ul>
     *   <li>Normal user — can see only HIS accounts</li>
     *   <li>Admin — can see ANY accounts</li>
     * </ul>
     *
     * @param accountNumber user account (20-digits string, unique ID)
     * @return {@link AccountResponse} with full information about account
     *
     * @throws ResourceNotFoundException if account with entered number doesn't found
     * @throws SecurityException if user not master of account or admin
     *
     * @see Account#getAccountNumber()
     * @see org.springframework.security.core.GrantedAuthority
     */
    @Transactional(readOnly = true)
    public AccountResponse getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !account.getUser().getEmail().equals(email)) {
            throw new SecurityException("You cannot access this account.");
        }

        return AccountResponse.fromEntity(account);
    }
    /**
     * Generate unique 20-digits number for Bank account.
     * <p>
     * <b>Algorithm:</b>
     * <ol>
     *   <li>Generate random 20-digit string from numbers</li>
     *   <li>Check if there is same account number in DB</li>
     *   <li>If true generate new account number (Cycle repeat)</li>
     *   <li>Return first unique number</li>
     * </ol>
     * </p>
     *
     * <p><b>Static:</b> The probability of a collision with 10⁴⁰ possible combinations is extremely low,
     * but the method guarantees uniqueness by checking in the database.</p>
     *
     * @return unique 20-digit account number (Example: "12345678901234567890")
     *
     * @see Account#setAccountNumber(String)
     * @see AccountRepository#existsByAccountNumber(String)
     */
    private String generateAccountNumber() {
        Random random = new Random();
        String accountNumber;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 20; i++) {
                sb.append(random.nextInt(10));
            }
            accountNumber = sb.toString();
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }
    @lombok.Data
    public static class CreateAccountRequest {
        private Account.AccountType type = Account.AccountType.CHECKING;
        private String currency = "RUB";
    }
    /**
     * Closes a bank account (makes it inactive).
     * <p>
     * <b>Conditions for closing an account:</b>
     * <ul>
     * <li>The account must exist</li>
     * <li>The account must be active (not previously closed)</li>
     * <li>The account balance must be 0</li>
     * <li>There must be no pending transactions with the PENDING status</li>
     * <li>The current user must be the account owner or administrator</li>
     * </ul>
     * </p>
     *
     * @param accountNumber account number to close
     * @throws ResourceNotFoundException if the account is not found
     * @throws IllegalStateException if the account is already closed, has a non-zero balance, or has active transactions
     * @throws SecurityException if the user is not the account owner or administrator
     */
    @Transactional

    public void closeAccount(String accountNumber) {
        log.info("Request for Account closure: {}", accountNumber);

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));

        if (!isAdmin && !account.getUser().getEmail().equals(email)) {
            log.warn("Attempt to close someone else's account: {} by user {}", accountNumber, email);
            throw new SecurityException("You do not have permission to close this account");
        }

        if (!account.isActive()) {
            log.warn("Attempt to close an already inactive account: {}", accountNumber);
            throw new IllegalStateException("Account already closed");
        }

        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            log.warn("Attempt to close an account with a non-zero balance: {}, balance: {}",
                    accountNumber, account.getBalance());
            throw new IllegalStateException(
                    String.format("Unable to close an account with a balance of %.2f %s." +
                                    "Please transfer or withdraw funds before closing.",
                            account.getBalance(), account.getCurrency())
            );
        }

        boolean hasPendingTransactions = checkPendingTransactions(account);
        if (hasPendingTransactions) {
            log.warn("Attempt to close an account with pending transactions: {}", accountNumber);
            throw new IllegalStateException(
                    "Unable to close the account. There are pending transactions." +
                            "Wait for them to complete or contact support."
            );
        }
        account.setActive(false);
        accountRepository.save(account);

        log.info("Account {} successfully closed by user {}", accountNumber, email);
    }

    /**
     * Checks if the account has pending transactions.
     *
     * @param account - the account to check
     * @return true if there are transactions with the PENDING status
     */
    private boolean checkPendingTransactions(Account account) {
        long pendingOutgoing = transactionRepository.countByFromAccountAndStatus(
                account, Transaction.TransactionStatus.PENDING);

        long pendingIncoming = transactionRepository.countByToAccountAndStatus(
                account, Transaction.TransactionStatus.PENDING);

        return pendingOutgoing > 0 || pendingIncoming > 0;
    }
}
