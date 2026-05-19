package com.Bank;

import com.Bank.InsufficientFundsException;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** The entity of Bank's account
 * <p>
 *     Bank's account, consisting of information about account's number,
 *     current balance, currency of account and type. Every account belong
 *     one user & can participate in transaction like sand/recive
 * </p>
 *
 * <p>Example: </p>
 * <pre>
 *     Account account = new Account();
 *     account.setAccountNumber("12345678901234567890");
 *     account.setBalance(new BigDecimal("10000.0"));
 *     account.setUser(user);
 *     accountRepository.save(account);
 * </pre>
 *
 * @author Valep Vinreo
 * @version 1.0
 * @see User
 * @see Transaction
 * @since 04-2025
 *
 */
@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * Unique 20-digit's account number.
     * Generate auto by creating Account.
     */
    @Column(unique = true, nullable = false)
    private String accountNumber;
    /**
     * User-master of account.
     * connect many-to-one: One user can have many account's.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    /**
     * Current account balance.
     * Use {@link BigDecimal} for storaging an accurate amount of money.
     * Can't be negative (checking at this method {@link #debit(BigDecimal)}).
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;
    /**
     * Account currency formate ISO 4217 (RUB,USD,EUR).
     */
    private String currency = "RUB";
    /**
     * The type of bank account.
     * @see AccountType
     */
    @Enumerated(EnumType.STRING)
    private AccountType type = AccountType.CHECKING;
    /**
     * Status of account: Active or Closed.
     * Closed bill's can't participate in transaction.
     */
    private boolean active = true;
    /**
     * Date/time of creating account.
     * Create by creating essence.
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Type of account.
     */
    public enum AccountType {
        CHECKING, SAVINGS, BUSINESS
    }

    /** write-off funds
     * <p>
     *     Reducing account balance by specified amount. before write-off checking:
     *     <ul>
     *         <li>Amount can't be null or negative.</li>
     *         <li>Account should have enough funds</li>
     *     </ul>
     * </p>
     *
     * @param amount Reducing amount (Should be more than 0)
     * @throws IllegalArgumentException if amount null/negative or equals 0
     * @throws InsufficientFundsException if account doesn't have enough funds
     */
    public void debit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("The write-off amount must be positive");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException(this.accountNumber, amount, this.balance);
        }
        this.balance = this.balance.subtract(amount);
    }

    /** write-on funds.
     *
     *
     * @param amount write-on amount (amount is bigger than zero)
     * @throws IllegalArgumentException if amount is null/negative or equal zero
     */
    public void credit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("The deposit amount must be positive.");
        }
        this.balance = this.balance.add(amount);
    }
}
