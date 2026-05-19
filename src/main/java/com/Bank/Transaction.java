package com.Bank;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * The entity of Bank transaction(funds transfer).
 * <p>Are record of transaction(any move of funds) between account's.
 * Any transaction has its unique ID, status & Date stamps.
 * </p>
 *
 * <p>
 *     <b>Living cycle of transaction:</b>
 *     <ul>
 *         <li><B>PENDING</B> - Request are maid. awaiting response;</li>
 *         <li><B>COMPLETED</B> - Successful complete. Money delivered;</li>
 *         <li><b>FAILED</b> - Any kind of error(not enough error, account not found & etc.);</li>
 *         <li><b>CANCELED</b> - Canceled by user or system.</li>
 *     </ul>
 *</p>
 *     <p><b>Connect with other's entity:</b></p>
 *     <ul>
 *         <li>{@link Account} - Account-sender. One-to many;</li>
 *         <li>{@link Account} - Account-receiver. Many-to-One;</li>
 *     </ul>
 *
 *     <p><b>Example of Transaction creating:</b></p>
 *     <pre>
 *      Transaction transaction = Transaction.builder()
 *         .transactionId(UUID.randomUUID().toString())
 *         .fromAccount(fromAccount)
 *         .toAccount(toAccount)
 *         .amount(new BigDecimal("100.00"))
 *         .type(TransactionType.TRANSFER)
 *         .description("Оплата интернета")
 *         .build();
 * transactionRepository.save(transaction);
 *     </pre>
 * @author Valep Vinreo
 * @version 1.o
 * @see Account
 * @see TransactionType
 * @see TransactionStatus
 * @since 04-2026
 */
@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    /**
     * Unique ID of transaction at DB.
     * Auto-incrementing field, primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * External Transaction Identifier (UUID).
     * <p>
     * Generated when a transaction is created and can be used
     * by external systems to track the transfer status.
     * </p>
     * <p>Format: 36 characters (e.g., {@code 550e8400-e29b-41d4-a716-446655440000})</p>
     */
    @Column(unique = true)
    private String transactionId;
    /**
     * The sending account.
     * <p>
     * Funds are debited from this account.
     * Can only be null for DEPOSIT transactions.
     * </p>
     *
     * @see #getToAccount()
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id")
    private Account fromAccount;
    /**
     * Recipient account.
     * <p>
     * Funds are credited to this account.
     * Can only be null for WITHDRAWAL transactions.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id")
    private Account toAccount;
    /**
     * Transfer amount (excluding commission).
     * <p>
     * Always a positive number. This is the amount that will be credited to the recipient's account.
     * </p>
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    /**
     * Operation type.
     *
     * @see TransactionType
     */
    @Enumerated(EnumType.STRING)
    private TransactionType type;
    /**
     * Current transaction status.
     *
     * @see TransactionStatus
     */
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
    /**
     * Description of operation purpose(Optional).
     *
     * <p>Can be seen at transaction history.</p>
     * <p>Blank if user doesn't input.</p>
     */
    private String description;
    /**
     * Transfer fee amount.
     * <p>
     * For In-bank transfers, usually 0.
     * For external transfers, it may be a percentage of the amount.
     * </p>
     */
    private BigDecimal fee;
    /**
     * Transaction creation date and time.
     * <p>
     * Set when the transfer is initiated (before execution).
     * Automatically populated before saving to the database.
     * </p>
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    /**
     * The date and time the transaction was completed.
     * <p>
     * Set only when the transaction status is set to COMPLETED.
     * For PENDING, FAILED, and CANCELLED statuses, null.
     * </p>
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    /**
     * Type of transaction.
     */
    public enum TransactionType {
        TRANSFER, DEPOSIT, WITHDRAWAL, FEE
    }
    /**
     * Status of transaction.
     */
    public enum TransactionStatus {
        PENDING, COMPLETED, FAILED, CANCELLED
    }

    /**
     * Automatically sets the creation date before saving.
     * <p>
     * This method is called by Hibernate before saving an entity for the first time.
     * </p>
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = TransactionStatus.PENDING;
        }
    }
    /**
     * Automatically sets the completion date when the transaction status changes COMPLETED.
     *
     * @see #setStatus(TransactionStatus)
     */
    @PreUpdate
    protected void onUpdate() {
        if (status == TransactionStatus.COMPLETED && completedAt == null) {
            completedAt = LocalDateTime.now();
        }
    }
}
