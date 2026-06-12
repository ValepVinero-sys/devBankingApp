package com.Bank;


import com.Bank.dto.request.TransferRequest;
import com.Bank.dto.response.TransactionResponse;
import com.Bank.exception.InsufficientFundsException;
import com.Bank.exception.ResourceNotFoundException;
import com.Bank.model.Account;
import com.Bank.repository.AccountRepository;
import com.Bank.repository.TransactionRepository;
import com.Bank.service.TransferService;
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
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Transfer service test")
class TransferServiceSimpleTest {

    @Mock private AccountRepository accountRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private TransferRequest request;
    @Mock private Account from;
    @Mock private Account to;
    @InjectMocks private TransferService transferService;

    @BeforeEach
    void setup(){
        request = new TransferRequest();
        request.setFromAccountNumber("22222222222222222222");
        request.setToAccountNumber("11111111111111111111");
        request.setAmount(BigDecimal.valueOf(200));
        request.setDescription("");
    }

    @Test
    @DisplayName("funds transfer success test")
    void transferMoneySuccess(){

        Random random = new Random();
        Long randonLongIDFM = random.nextLong();
        Long randonLongIDT = random.nextLong();
        from = new Account();
        to = new Account();
        from.setId(randonLongIDFM);
        to.setId(randonLongIDT);

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

        Random random = new Random();
        Long randonLongIDFM = random.nextLong();
        Long randonLongIDT = random.nextLong();
        from = new Account();
        to = new Account();
        from.setId(randonLongIDFM);
        to.setId(randonLongIDT);

        from.setBalance(BigDecimal.valueOf(100));
        to.setBalance(BigDecimal.valueOf(0));

        when(accountRepository.findForUpdate("22222222222222222222")).thenReturn(Optional.of(from));
        when(accountRepository.findForUpdate("11111111111111111111")).thenReturn(Optional.of(to));

        assertThatThrownBy(() -> transferService.transferMoney(request))
                .isInstanceOf(InsufficientFundsException.class);
    }
    //Failed test due to short time
    @Test
    void Transfer_Not_Found_ACC_Sender(){
        when(accountRepository.findForUpdate("11111111111111111111"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.transferMoney(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Sender account not found");
    }
    @Test
    void Transfer_TOO_SAME_ACC(){

        from.setBalance(BigDecimal.valueOf(200));
        to.setBalance(BigDecimal.valueOf(0));

        request.setToAccountNumber("22222222222222222222");

        when(accountRepository.findForUpdate("22222222222222222222")).thenReturn(Optional.of(from));
        when(accountRepository.findForUpdate("22222222222222222222")).thenReturn(Optional.of(to));

        assertThatThrownBy(() -> transferService.transferMoney(request)).isInstanceOf(IllegalArgumentException.class);

    }
    //Failed test due to short time
    @Test
    void transfer_NotFound_ReceiverAccount() {

        when(accountRepository.findForUpdate("11111111111111111111"))
                .thenReturn(Optional.of(from));
        when(accountRepository.findForUpdate("22222222222222222222"))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> transferService.transferMoney(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Recipient account not found");
    }

}
