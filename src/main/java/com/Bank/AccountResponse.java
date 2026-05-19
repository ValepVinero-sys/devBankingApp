package com.Bank;

import com.Bank.Account;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AccountResponse {
    private Long id;
    private String accountNumber;
    private BigDecimal balance;
    private String type;
    private String currency;
    private LocalDateTime createdAt;
    private boolean isActive;
    private Long userId;
    private String userEmail;

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