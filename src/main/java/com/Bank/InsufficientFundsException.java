package com.Bank;

import org.aspectj.bridge.Message;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {
    private final String accountNumber;
    private final BigDecimal requestedAmount;
    private final BigDecimal availableBalance;

    public InsufficientFundsException(String accountNumber,
                                      BigDecimal requestedAmount,
                                      BigDecimal availableBalance) {
        super(String.format("Недостаточно средств на счёте %s. Запрошено: %s, доступно: %s",
                accountNumber, requestedAmount, availableBalance));
        this.accountNumber = accountNumber;
        this.requestedAmount = requestedAmount;
        this.availableBalance = availableBalance;
    }
}
