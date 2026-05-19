package com.Bank;

import com.Bank.AccountResponse;
import com.Bank.ResourceNotFoundException;
import com.Bank.Account;
import com.Bank.User;
import com.Bank.AccountRepository;
import com.Bank.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        // Создаём новый счёт, а не кастуем коллекцию!
        Account account = new Account();
        account.setAccountNumber(generateAccountNumber());
        account.setUser(user);
        account.setType(request.getType());
        account.setCurrency(request.getCurrency());
        account.setBalance(java.math.BigDecimal.ZERO);

        Account savedAccount = accountRepository.save(account);
        return AccountResponse.fromEntity(savedAccount);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getUserAccounts() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        return accountRepository.findByUser(user).stream()
                .map(AccountResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Счёт не найден"));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !account.getUser().getEmail().equals(email)) {
            throw new SecurityException("Нет доступа к этому счёту");
        }

        return AccountResponse.fromEntity(account);
    }

    private String generateAccountNumber() {
        Random random = new Random();
        String accountNumber;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 20; i++) {
                sb.append(random.nextInt(10));
            }
            accountNumber = sb.toString();
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }

    // DTO для запроса создания счёта
    @lombok.Data
    public static class CreateAccountRequest {
        private Account.AccountType type = Account.AccountType.CHECKING;
        private String currency = "RUB";
    }
}