package com.Bank;

import com.Bank.Account;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for response request about Bank account information.
 * <p>
 *     Used at next endpoints:
 *     <ul>
 *         <li>{@code GET /api/accounts} - List of all user account's</li>
 *         <li>{@code GET /api/accounts/{accountNumber}} - Detailed information about account</li>
 *         <li>{@code POST /api/accounts} - Creating of new account</li>
 *     </ul>
 * </p>
 *
 * <p>Example of JSON-Response:</p>
 * <pre>{
 *   "id": 1,
 *   "accountNumber": "12345678901234567890",
 *   "balance": 1000.50,
 *   "type": "CHECKING",
 *   "currency": "RUB",
 *   "createdAt": "2025-01-15T10:30:00",
 *   "isActive": true,
 *   "userId": 42,
 *   "userEmail": "user@example.com"}
 * </pre>
 * @author Valep Vinreo
 * @version 1.0
 * @see com.Bank.Account
 * @see com.Bank.Account_Controller
 * @since 04-2026
 */
@Data
@Builder
public class AccountResponse {
    /**
     * Unique account id at DB.
     * (Inner ID. Doesn't show at user interface)
     */
    private Long id;
    /**
     * Bank account number.
     * <p>
     * Unique 20-digit identifier generated when creating an account.
     * Used to identify the account in the API and for transfers.
     * </p>
     * <p>Format: 20 digits (e.g., 12345678901234567890)</p>
     */
    private String accountNumber;
    /**
     * Current account balance.
     * <p>
     * Always positive or zero (negative balances are prohibited).
     * For precise storage of funds amounts, the {@link BigDecimal} type is used.
     * </p>
     * <p>Format: up to 15 decimal places, 2 decimal places (kopecks/cents)</p>
     */
    private BigDecimal balance;
    /**
     * Bank account type.
     * <p>
     * Possible values:
     * <ul>
     * <li><b>CHECKING</b> — checking account for everyday transactions</li>
     * <li><b>SAVINGS</b> — savings account (may accrue interest)</li>
     * <li><b>BUSINESS</b> — business account for sole proprietors and legal entities</li>
     * </ul>
     * </p>
     *
     * @see Account.AccountType
     */
    private String type;
    /**
     * Account currency in ISO 4217 format.
     * <p>
     * Supported currencies:
     * <ul>
     * <li>RUB — Russian ruble</li>
     * <li>USD — US dollar</li>
     * <li>EUR — Euro</li>
     * </ul>
     * </p>
     */
    private String currency;
    /**
     * Date & time for account creation.
     * <p>
     * format ISO 8601: {@code yyyy-MM-dd'T'HH:mm:ss}
     * </p>
     * <p>Example: {@code 2025-01-15T10:30:00}</p>
     */
    private LocalDateTime createdAt;
    /**
     * Activity status of account.
     * <p>
     * <ul>
     *   <li>{@code true} — account active, operation's is permitted</li>
     *   <li>{@code false} — account closed, operations are not allowed</li>
     * </ul>
     * </p>
     */
    private boolean isActive;
    /**
     * ID of user-owner of account.
     * (Inner id. Doesn't show-up at user's interface but can be used for link).
     */
    private Long userId;
    /**
     * User-owner email.
     * <p>
     * Added for easer identification of owner without separated request to user.
     * </p>
     */
    private String userEmail;
    /**
     * Converts an {@link Account} entity to a {@link AccountResponse} DTO.
     * <p>
     * This is a factory method that:
     * <ul>
     * <li>Copies all fields from the JPA entity to the DTO.</li>
     * <li>Eliminates circular references (e.g., account → user → list of accounts).</li>
     * <li>Prevents {@link org.hibernate.LazyInitializationException}.</li>
     * </ul>
     * </p>
     *
     * @param account: JPA account entity (cannot be null).
     * @return DTO to send to the client.
     * @throws NullPointerException if account == null.
     *
     * @see Account
     */
    public static AccountResponse fromEntity(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .type(account.getType().name())
                .currency(account.getCurrency())
                .createdAt(account.getCreatedAt())
                .isActive(account.isActive())
                .userId(account.getUser().getId())
                .userEmail(account.getUser().getEmail())
                .build();
    }
}
