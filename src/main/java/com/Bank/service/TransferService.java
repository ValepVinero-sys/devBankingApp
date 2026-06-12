package com.Bank.service;

import com.Bank.dto.request.TransferRequest;
import com.Bank.dto.response.TransactionResponse;
import com.Bank.exception.InsufficientFundsException;
import com.Bank.exception.ResourceNotFoundException;
import com.Bank.model.Account;
import com.Bank.model.Transaction;
import com.Bank.repository.AccountRepository;
import com.Bank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for processing transfer of money.
 * <p>
 *     Realize business-logic of transfer between account's,
 *     including check of balance, close account & saving transaction history.
 * </p>
 *
 * <p>
 *     All operation processing through single transaction with automatically
 *     abort possibility if there is any exception throws.
 * </p>
 * @author Valep Vonery
 * @version 1.00
 * @see AccountService
 * @see TransactionService
 * @since 04-2026
 *
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    /** perform transfer between two accounts.
     * <p>
     *     Algorithm:
     *     <ol>
     *         <li> Receiving account's with pessimistic lock(FOR UPDATE)</li>
     *         <li> Validating: checking existence, activity, currency, enough funds</li>
     *
     *         <li> Making a record of transaction with PENDING status </li>
     *         <li> debit/credit funds </li>
     *         <li> Updating status of transaction to COMPLETED</li>
     *     </ol>
     * </p>
     *
     *
     * @param request object with data for transfer (sender/receiver, amount, description)
     * @return object {@link TransactionResponse} with detail's of completed transaction
     *
     * @throws ResourceNotFoundException if sender's/receiver account not found in database
     * @throws IllegalArgumentException in the following cases:
     *      <ul>
     *          <li>Attempt transfer to the same account</li>
     *          <li>One of account for transfer inactive</li>
     *          <li>accounts have different currency</li>
     *      </ul>
     * @throws InsufficientFundsException if sender's account doesn't have enough funds
     * @throws RuntimeException if unexpected error has occurred during transfer
     *
     * @see TransferRequest
     * @see TransactionResponse
     *
     */
    @Transactional(rollbackFor = Exception.class)
    public TransactionResponse transferMoney(TransferRequest request) {
        log.info("Transfer Start: {} -> {}, Amount: {}",
                request.getFromAccountNumber(),
                request.getToAccountNumber(),
                request.getAmount());

        Account fromAccount = accountRepository.findForUpdate(request.getFromAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Sender account not found"));

        Account toAccount = accountRepository.findForUpdate(request.getToAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Recipient account not found"));

        if (fromAccount.getId().equals(toAccount.getId())) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        if (!fromAccount.isActive() || !toAccount.isActive()) {
            throw new IllegalArgumentException("One of the accounts is inactive");
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

            log.info("Transfer completed successfully. Transaction ID: {}", transaction.getId());
            return TransactionResponse.fromEntity(transaction);

        } catch (InsufficientFundsException e) {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            log.error("Transfer failed: insufficient funds", e);
            throw e;
        } catch (Exception e) {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            log.error("Unexpected error while transferring", e);
            throw new RuntimeException("Error while executing transfer", e);
        }
    }
    /** Calculate transaction fee. currently not implemented in project.
     * <p>
     *     At this version:
     *     <ul>
     *         <li>in-Bank transfer (internal) - free</li>
     *         <li>out-Bank transfer (external) - fee is 1% from amount</li>
     *     </ul>
     * </p>
     *
     * @param amount Transfer amount
     * @param transferType Type of transfer (internal/external)
     * @return amount of fee at the same currency
     */
    //Not implemented yet. Saved for future
    @Transactional(readOnly = true)
    public BigDecimal calculateFee(BigDecimal amount, String transferType) {
        if ("INTERNAL".equals(transferType)) {
            return BigDecimal.ZERO;
        } else {
            return amount.multiply(new BigDecimal("0.01"));
        }
    }
}