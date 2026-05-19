package com.Bank;

import org.aspectj.bridge.Message;

import java.math.BigDecimal;

/**
 * An exception thrown when attempting to debit funds from an account,
 * when the available balance is less than the requested amount.
 * <p>
 *     This business exception occurs in the following situations:
 *     <ul>
 *         <li>Transferring funds in excess of the available balance</li>
 *         <li>Cash withdrawal in an amount exceeding the account balance</li>
 *         <li>Payment for services exceeding the account limit</li>
 *     </ul>
 *   </p>
 *     <p><b>Example of use:</b></p>
 *     <pre>
 *     public void debit(BigDecimal amount) {
 *     if (balance.compareTo(amount) < 0) {
 *         throw new InsufficientFundsException(accountNumber, amount, balance);
 *     }
 *     // ... write-off of funds
 * }
 *     </pre>
 *     <p><b>Handling an exception in a global handler:</b></p>
 *     <pre>
 *         {@code @ExceptionHandler(InsufficientFundsException.class)
 *      public ResponseEntity<ErrorResponse> handleInsufficientFunds(InsufficientFundsException ex) {
 *return ResponseEntity.badRequest()
 *          .body(ErrorResponse.builder()
 *              .message(ex.getMessage())
 *              .details(Map.of(
 *                  "accountNumber", ex.getAccountNumber(),
 *                  "requestedAmount", ex.getRequestedAmount(),
 *                  "availableBalance", ex.getAvailableBalance()
 *              ))
 *              .build());
 *  }}</pre>
 * @author Valep Vinreo
 * @version 1.0
 * @see ResourceNotFoundException
 * @see Global_Exception_Handler
 * @since 04-2026
 */
public class InsufficientFundsException extends RuntimeException {
    /**
     * The account number where the error occurred.
     * <p>
     * Used for logging and generating an informative message.
     * </p>
     */
    private final String accountNumber;
    /**
     * The amount they attempted to write off.
     * <p>
     * Always a positive number.
     * </p>
     */
    private final BigDecimal requestedAmount;
    /**
     * Available balance at the time of the transaction.
     * <p>
     * Always less than the requested amount.
     * </p>
     */
    private final BigDecimal availableBalance;
    /**
     * Raises an exception with full error information.
     * <p>
     * Generates a detailed message indicating the account number,
     * requested amount, and available balance.
     * </p>
     *
     * @param accountNumber account number (cannot be null)
     * @param requestedAmount requested debit amount (must be > 0)
     * @param availableBalance current account balance (must be < requestedAmount)
     *
     * @throws NullPointerException if any of the parameters is null
     *
     * @see #getMessage()
     */
    public InsufficientFundsException(String accountNumber,
                                      BigDecimal requestedAmount,
                                      BigDecimal availableBalance) {
        super(String.format("Not enough funds on account %s. Requested: %s, Available: %s",
                accountNumber,
                requestedAmount != null ? requestedAmount : "not specified",
                availableBalance != null ? availableBalance : "Unknown"));

        this.accountNumber = accountNumber;
        this.requestedAmount = requestedAmount;
        this.availableBalance = availableBalance;
    }
    /**
     * Creates an exception with only a message (no additional fields).
     * <p>
     * Used when there is no account number or amount information.
     * </p>
     *
     * @param message error message
     */
    // Not implemented yet, reserved for future use
    public InsufficientFundsException(String message) {
        super(message);
        this.accountNumber = null;
        this.requestedAmount = null;
        this.availableBalance = null;
    }
    /**
     * Creates an exception with a message and cause.
     * @param message - the error message
     * @param cause - the original cause of the exception
     */
    // Not implemented yet, reserved for future use
    public InsufficientFundsException(String message, Throwable cause) {
        super(message, cause);
        this.accountNumber = null;
        this.requestedAmount = null;
        this.availableBalance = null;
    }
    /**
     * Returns the difference between the requested amount and the available balance.
     * <p>
     * Useful for generating messages like:
     * "You are missing X units to complete the transaction."
     * </p>
     *
     * @return the missing amount (always positive), or null if the data is missing.
     */
    // Not implemented yet, reserved for future use
    public BigDecimal getDeficitAmount() {
        if (requestedAmount == null || availableBalance == null) {
            return null;
        }
        return requestedAmount.subtract(availableBalance);
    }
}
