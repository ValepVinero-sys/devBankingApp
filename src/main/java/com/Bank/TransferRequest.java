package com.Bank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/** DTO (data transfer object) for request transfer of funds
 * <p>
 *     Used at {@link TransferService#transferMoney(TransferRequest)}
 *     for data transfer from controller to service.
 * </p>
 *
 * @author Valep Vinreo
 * @version 1.00
 * @see TransferService
 * @since 04-2026
 *
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class TransferRequest {
        /**
         * Required sender's account number.
         * Cannot be null.
         */
        @NotBlank(message = "Account number of sender is required ")
        private String fromAccountNumber;
        /**
         * Required receiver account number.
         * Cannot be null.
         */
        @NotBlank(message = "Bill number of receiver is required")
        private String toAccountNumber;
        /**
         * Required amount of funds.
         * Cannot be null.
         */
        @NotNull(message = "Amount required")
        @DecimalMin(value = "0.01", message = "Amount should be higher than 0")
        private BigDecimal amount;
        public BigDecimal get_amount(){return amount;}

        /**
         * Description purpose of transfer (optional).
         * Can be seen at transfer history.
         */
        private String description;
    }
