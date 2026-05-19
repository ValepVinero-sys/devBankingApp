package com.Bank;

import com.Bank.Account;
import com.Bank.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByFromAccountOrToAccountOrderByCreatedAtDesc(
            Account fromAccount, Account toAccount, Pageable pageable);

    List<Transaction> findByFromAccountAndCreatedAtBetween(
            Account account, LocalDateTime start, LocalDateTime end);

    @Query("SELECT t FROM Transaction t WHERE " +
            "t.fromAccount.accountNumber = :accountNumber OR " +
            "t.toAccount.accountNumber = :accountNumber ORDER BY t.createdAt DESC")
    List<Transaction> findTransactionsByAccountNumber(@Param("accountNumber") String accountNumber);

    long countByStatus(Transaction.TransactionStatus status);
}