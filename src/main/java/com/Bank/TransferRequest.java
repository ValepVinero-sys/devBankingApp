package com.Bank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
@EqualsAndHashCode(callSuper = false)
@Data
public class TransferRequest {
        @NotBlank(message = "Bill number of sender is required ")
        private String fromAccountNumber;
        @NotBlank(message = "Bill number of reciver is required")
        private String toAccountNumber;
        @NotNull(message = "amount reqired")
        @DecimalMin(value = "0.01", message = "amount should be higher than 0")
        private BigDecimal amount;
        public BigDecimal get_amount(){return amount;}
        private String description;
    }
