package com.Bank;
import com.Bank.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * DTO for the response when performing a money transfer.
 * <p>
 * Returned after a successful transfer via the endpoint
 * {@code POST /api/accounts/transfer}.
 * </p>
 *
 * <p>JSON response example:</p>
 * <pre>
 * {
 * "id": 100500,
 * "transactionId": "550e8400-e29b-41d4-a716-446655440000",
 * "fromAccountNumber": "111111111111111111111",
 * "toAccountNumber": "22222222222222222222",
 * "amount": 500.00,
 * "type": "TRANSFER",
 * "status": "COMPLETED",
 * "description": "Internet payment",
 * "fee": 0.00,
 * "createdAt": "2025-01-15T14:25:00",
 * "completedAt": "2025-01-15T14:25:01"
 * }
 * </pre>
 *
 * @author Valep Vinreo
 * @version 1.0
 * @see Transaction
 * @see TransferController
 * @since 04-2026
 */
public class TransactionResponse {
    /**
     * Unique ID of transaction at Data Base
     */
    private Long id;
    /**
     * Outer ID of transaction (UUID).
     * <p>
     *     Generate when transaction is created &
     *     can be used for tracing status of transaction at outer systems (Transaction history).
     * </p>
     * <p>Format: Usual UUID (Example: 550e8400-e29b-41d4-a716-446655440000) </p>
     */
    private String transactionId;
    /**
     * Account number of sender
     * <p>Funds are written-off from this account</p>
     */
    private String fromAccountNumber;
    /**
     * Account of receiver.
     * <p>Funds are written-on to this account</p>
     */
    private String toAccountNumber;
    /**
     * Funds amount (Without fee).
     * <p>Always positive number. This amount is written to account</p>
     */
    private BigDecimal amount;
    /**
     * Type of transaction.
     * <p>
     *     Possible type:
     *     <ul>
     *         <li><b>TRANSFER</b> - transfer funds from one account to another;</li>
     *         <li><b>DEPOSIT</b> - add funds to account (Outer operation);</li>
     *         <lI><b>WITHDRAWAL</b> - withdraw fuds from account(outer operation).</lI>
     *     </ul>
     * </p>
     */
    private String type;
    /**
     * status of transaction
     * <p>
     *     Possible status:
     *     <ul>
     *         <li><b>PENDING</b> - in processing (awaiting confirmation);</li>
     *         <li><b>COMPLETED</b> - transfer of funds was successful(completed);</li>
     *         <li><b>FAILED</b> - Canceled by user/system.</li>
     *     </ul>
     * </p>
     */
    private String status;
    /**
     * description of Transaction purpose(optional).
     * <p>Blank by default. Can be seen at transaction history.</p>
     */
    private String description;
    /**
     * Fee of transaction.
     * <p>
     *     At current version:
     *     <ul>
     *  <li>in-bank transfers — 0.00</li>
     *  <li>External transfers — 1% of the amount</li>
     *  </ul>
     *  The fee is debited as a separate transaction with the {@code FEE} type.
     *  </p>
     */
    private BigDecimal fee;
    /**
     * The date & time the transaction was created.
     *  <p>
     *  Set at the time the transfer is initiated (before execution).
     *  ISO 8601 format.
     * </p>
     */
    private LocalDateTime createdAt;
    /**
     * The date & time the transaction was completed.
     *  <p>
     *  set only for transaction with status {@code COMPLETED}
     *  can be null for transaction with status {@code PENDING/FAILED}
     * </p>
     */
    private LocalDateTime completedAt;
    /**
     * Converts a {@link Transaction} entity to a {@link TransactionResponse} DTO.
     * <p>
     * Factory method that:
     * <ul>
     * <li>Extracts account numbers from related {@link Account} objects</li>
     * <li>Converts enum values to string values</li>
     * <li>Handles possible null values for {@code completedAt}</li>
     * </ul>
     * </p>
     * <p>
     * <b>Important:</b> This assumes that related accounts have already been loaded
     * (use {@code JOIN FETCH} on the repository or the {@code @EntityGraph} annotation).
     * </p>
     *
     * @param transaction JPA transaction entity (cannot be null)
     * @return DTO to send to the client
     * @throws NullPointerException if transaction == null
     *
     * @see Transaction
     * @see Account
     */
    public static TransactionResponse fromEntity(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .transactionId(transaction.getTransactionId())
                .fromAccountNumber(transaction.getFromAccount() != null ?
                        transaction.getToAccount().getAccountNumber() : null)
                .toAccountNumber(transaction.getFromAccount() != null ?
                        transaction.getToAccount().getAccountNumber() : null)
                .amount(transaction.getAmount())
                .type(transaction.getType().name())
                .status(transaction.getStatus().name())
                .description(transaction.getDescription())
                .fee(transaction.getFee())
                .createdAt(transaction.getCreatedAt())
                .completedAt(transaction.getCompletedAt())
                .build();
    }
}
