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
public class TransactionResponse {
    private Long id;
    private String transactionId;      // UUID для внешних систем
    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal amount;
    private String type;               // TRANSFER, DEPOSIT, WITHDRAWAL
    private String status;             // PENDING, COMPLETED, FAILED
    private String description;
    private BigDecimal fee;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    // Статический фабричный метод для преобразования из Entity
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