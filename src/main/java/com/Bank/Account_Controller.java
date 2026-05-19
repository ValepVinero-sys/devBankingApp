package com.Bank;

import com.Bank.TransferRequest;
import com.Bank.AccountResponse;
import com.Bank.TransactionResponse;
import com.Bank.AccountService;
import com.Bank.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class Account_Controller {

    private final AccountService accountService;
    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody AccountService.CreateAccountRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getUserAccounts() {
        List<AccountResponse> accounts = accountService.getUserAccounts();
        return ResponseEntity.ok(accounts);  // ← возвращаем List, не кастуем
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String accountNumber) {
        AccountResponse response = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request) {
        TransactionResponse response = transferService.transferMoney(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable String accountNumber) {
        AccountResponse account = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(account.getBalance());
    }

    @DeleteMapping("/{accountNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> closeAccount(@PathVariable String accountNumber) {
        // Логика закрытия счёта
        return ResponseEntity.noContent().build();
    }
}