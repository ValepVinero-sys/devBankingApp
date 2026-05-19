package com.Bank;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Random;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Transfer service test")
class TransferServiceTest {

    @Mock private com.Bank.AccountRepository accountRepository;
    @Mock private com.Bank.TransactionRepository transactionRepository;
    @Mock private com.Bank.TransferRequest request;
    @Mock private com.Bank.Account from;
    @Mock private com.Bank.Account to;
    @InjectMocks private com.Bank.TransferService transferService;

    @BeforeEach
    void setup(){
        Random random = new Random();
        Long randonLongIDFM = random.nextLong();
        Long randonLongIDT = random.nextLong();
        from = new Account();
        to = new Account();
        from.setId(randonLongIDFM);
        to.setId(randonLongIDT);
        request = new TransferRequest();
        request.setFromAccountNumber("22222222222222222222");
        request.setToAccountNumber("11111111111111111111");
        request.setAmount(BigDecimal.valueOf(200));
        request.setDescription("");
    }

    @Test
    @DisplayName("funds transfer success test")
    void transferMoneySuccess(){

        from.setBalance(BigDecimal.valueOf(300));
        to.setBalance(BigDecimal.valueOf(0));

        when(accountRepository.findForUpdate("22222222222222222222")).thenReturn(Optional.of(from));
        when(accountRepository.findForUpdate("11111111111111111111")).thenReturn(Optional.of(to));

        TransactionResponse response = transferService.transferMoney(request);

        assertThat(response.getStatus()).isEqualTo("COMPLETED");
        verify(accountRepository, times(1)).save(from);
        verify(accountRepository, times(1)).save(to);
    }
    @Test
    void Transfer_Not_Enough_Funds(){

        from.setBalance(BigDecimal.valueOf(100));
        to.setBalance(BigDecimal.valueOf(0));

        when(accountRepository.findForUpdate("22222222222222222222")).thenReturn(Optional.of(from));
        when(accountRepository.findForUpdate("11111111111111111111")).thenReturn(Optional.of(to));

        TransactionResponse response = transferService.transferMoney(request);

        assertThat(response.getStatus()).isEqualTo("FAILED");
        verify(accountRepository, times(1)).save(from);
        verify(accountRepository, times(1)).save(to);

    }
    @Test
    void Transfer_Not_Found_ACC(){

        from.setBalance(BigDecimal.valueOf(200));
        to.setBalance(BigDecimal.valueOf(0));

        when(accountRepository.findForUpdate("22222222222222222220")).thenReturn(Optional.of(from));
        when(accountRepository.findForUpdate("11111111111111111112")).thenReturn(Optional.of(to));

        TransactionResponse response = transferService.transferMoney(request);

        assertThat(response.getStatus()).isEqualTo("FAILED");
        verify(accountRepository, times(1)).save(from);
        verify(accountRepository, times(1)).save(to);

    }
    @Test
    void Transfer_TOO_SAME_ACC(){

        from.setBalance(BigDecimal.valueOf(200));
        to.setBalance(BigDecimal.valueOf(0));

        request.setToAccountNumber("22222222222222222222");

        when(accountRepository.findForUpdate("22222222222222222222")).thenReturn(Optional.of(from));
        when(accountRepository.findForUpdate("22222222222222222222")).thenReturn(Optional.of(to));

        TransactionResponse response = transferService.transferMoney(request);

        assertThat(response.getStatus()).isEqualTo("FAILED");
        verify(accountRepository, times(1)).save(from);
        verify(accountRepository, times(1)).save(to);

    }

}
