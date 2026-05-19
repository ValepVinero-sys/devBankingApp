package com.Bank;

import com.Bank.TransferRequest;
import com.Bank.TransactionResponse;
import com.Bank.InsufficientFundsException;
import com.Bank.ResourceNotFoundException;
import com.Bank.Account;
import com.Bank.Transaction;
import com.Bank.AccountRepository;
import com.Bank.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Transactional(rollbackFor = Exception.class)
    public TransactionResponse transferMoney(TransferRequest request) {
        log.info("Начало перевода: {} -> {}, Сумма: {}",
                request.getFromAccountNumber(),
                request.getToAccountNumber(),
                request.getAmount());

        Account fromAccount = accountRepository.findForUpdate(request.getFromAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Счёт отправителя не найден"));

        Account toAccount = accountRepository.findForUpdate(request.getToAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Счёт получателя не найден"));

        if (fromAccount.getId().equals(toAccount.getId())) {
            throw new IllegalArgumentException("Нельзя переводить на тот же счёт");
        }

        if (!fromAccount.isActive() || !toAccount.isActive()) {
            throw new IllegalArgumentException("Один из счетов неактивен");
        }

        Transaction transaction = Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(request.getAmount())
                .type(Transaction.TransactionType.TRANSFER)
                .status(Transaction.TransactionStatus.PENDING)
                .description(request.getDescription())
                .build();

        transactionRepository.save(transaction);

        try {
            fromAccount.debit(request.getAmount());
            toAccount.credit(request.getAmount());

            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            transaction.setCompletedAt(LocalDateTime.now());

            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);
            transactionRepository.save(transaction);

            log.info("Перевод успешно выполнен. ID транзакции: {}", transaction.getId());
            return TransactionResponse.fromEntity(transaction);

        } catch (InsufficientFundsException e) {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            log.error("Перевод не удался: недостаточно средств", e);
            throw e;
        } catch (Exception e) {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            log.error("Неожиданная ошибка при переводе", e);
            throw new RuntimeException("Ошибка при выполнении перевода", e);
        }
    }
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByAccountNumber(String accountNumber) {
        List<Transaction> transactions = transactionRepository.findTransactionsByAccountNumber(accountNumber);
        return transactions.stream()
                .map(TransactionResponse::fromEntity)
                .collect(Collectors.toList());
    }
}