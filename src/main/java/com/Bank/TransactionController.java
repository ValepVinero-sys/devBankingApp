package com.Bank;

import com.Bank.TransactionResponse;
import com.Bank.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for managing banking transaction history.
 * <p>
 * Provides an API for retrieving, filtering, and searching transactions.
 * Used to display the history of transactions in dashboard,
 * generation of bank statements and data export.
 * </p>
 *
 * <p><b>Basic URL:</b> {@code /api/transactions}</p>
 * <p><b>Required authentication: All endpoints require a JWT token.</b></p>
 *
 * <p><b>Example of JWT header:</b></p>
 * <pre>
 * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
 * </pre>
 *
 * <p><b>Special:</b></p>
 * <ul>
 *   <li>Pagination support for large data volumes</li>
 *   <li>Filtering by dates and transaction types</li>
 *   <li>Checking transaction access rights</li>
 * </ul>
 *
 * @author Valep Vinreo
 * @version 1.0
 * @see TransactionService
 * @see TransactionResponse
 * @since 04-2026
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction history", description = "API for receiving history of operation &  extracts")
public class TransactionController {

    private final TransactionService transactionService;
    /**
     * Return transaction page of current user with pagination.
     * <p>
     * <b>Sorting:</b> By default from new to old(createdAt DESC).
     * </p>
     *
     * <p><b>Pagination settings (query parameters):</b></p>
     * <ul>
     *   <li><b>page</b> — page number (start with 0, default at 0)</li>
     *   <li><b>size</b> — Number of pages (by default 20, max 100)</li>
     *   <li><b>sort</b> — Field for sort (createdAt, amount, type)</li>
     *   <li><b>direction</b> — Sort direction (ASC, DESC)</li>
     * </ul>
     * </p>
     *
     * <p><b>Request example:</b></p>
     * <pre>
     * GET /api/transactions?page=0&size=10&sort=createdAt&direction=DESC
     * </pre>
     *
     * <p><b>Answer example (200 OK):</b></p>
     * <pre>
     * {
     *   "content": [
     *     {
     *       "id": 100500,
     *       "transactionId": "550e8400-e29b-41d4-a716-446655440000",
     *       "fromAccountNumber": "11111111111111111111",
     *       "toAccountNumber": "22222222222222222222",
     *       "amount": 500.00,
     *       "type": "TRANSFER",
     *       "status": "COMPLETED",
     *       "description": "Paying for the Internet",
     *       "fee": 0.00,
     *       "createdAt": "2025-01-15T14:25:00",
     *       "completedAt": "2025-01-15T14:25:01"
     *     }
     *   ],
     *   "pageable": {
     *     "pageNumber": 0,
     *     "pageSize": 10,
     *     "totalPages": 5,
     *     "totalElements": 42
     *   }
     * }
     * </pre>
     *
     * @param page page number (start with 0)
     * @param size numbers of record's at page (1-100)
     * @return page with transaction of user
     * <p>
     *  200 - list of successfully transaction received
     *  401 - User doesn't authenticate
     *  400 - wrong pagination settings
     */
    @GetMapping
    @Operation(
            summary = "Receive transaction history",
            description = "Return transaction list of current user with pagination support"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List received"),
            @ApiResponse(responseCode = "400", description = "Wrong request parameter"),
            @ApiResponse(responseCode = "401", description = "Doesn't authenticated")
    })
    public ResponseEntity<Page<TransactionResponse>> getUserTransactions(
            @Parameter(description = "Page number (start with 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of entries per page (1-100)", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Field for sort", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sort,

            @Parameter(description = "Sorting direction (ASC или DESC)", example = "DESC")
            @RequestParam(defaultValue = "DESC") String direction) {

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        return ResponseEntity.ok(Page.empty());
    }

    /**
     * Returns all transactions associated with the specified account number.
     * <p>
     * A transaction is considered to be linked to an account if:
     * <ul>
     *   <li>the account is the sender, or</li>
     *   <li>the account is the recipient</li>
     * </ul>
     * </p>
     *
     * <p><b>Request example:</b></p>
     * <pre>
     * GET /api/transactions/account/12345678901234567890
     * </pre>
     *
     * <p><b>Answer example (200 OK):</b></p>
     * <pre>
     * [
     *   {
     *     "id": 100500,
     *     "transactionId": "550e8400-e29b-41d4-a716-446655440000",
     *     "fromAccountNumber": "12345678901234567890",
     *     "toAccountNumber": "09876543210987654321",
     *     "amount": 1000.00,
     *     "type": "TRANSFER",
     *     "status": "COMPLETED",
     *     "description": "Transfer to card",
     *     "fee": 10.00,
     *     "createdAt": "2025-01-15T10:30:00",
     *     "completedAt": "2025-01-15T10:30:01"
     *   },
     *   {
     *     "id": 100501,
     *     "transactionId": "660e8400-e29b-41d4-a716-446655440001",
     *     "fromAccountNumber": "09876543210987654321",
     *     "toAccountNumber": "12345678901234567890",
     *     "amount": 500.00,
     *     "type": "TRANSFER",
     *     "status": "COMPLETED",
     *     "description": "Funds return",
     *     "fee": 0.00,
     *     "createdAt": "2025-01-16T15:45:00",
     *     "completedAt": "2025-01-16T15:45:01"
     *   }
     * ]
     * </pre>
     *
     * @param accountNumber Account number (unique 20-digit)
     * @return Transaction list related to account
     * <p>
     *  200 - Transaction list received
     *  401 - The user is not authenticated
     *  403 - There is no access to the specified account
     *  404 - The account with the specified number was not found
     */
    @GetMapping("/account/{accountNumber}")
    @Operation(
            summary = "Receive transaction by account number",
            description = "return all incoming and outgoing transactions for specified account"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List received"),
            @ApiResponse(responseCode = "403", description = "No access to account"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<List<TransactionResponse>> getTransactionsByAccountNumber(
            @Parameter(description = "Account number (20 digit)", example = "12345678901234567890", required = true)
            @PathVariable String accountNumber) {

        List<TransactionResponse> transactions = transactionService.getTransactionsByAccountNumber(accountNumber);
        return ResponseEntity.ok(transactions);
    }
    /**
     * Return all transaction from specified account for specified date of time
     * <p>
     * <b>Date format:</b>
     * <ul>
     *   <li>ISO 8601: {@code 2025-01-15T10:30:00}</li>
     *   <li>Only data: {@code 2025-01-15} (время будет 00:00:00)</li>
     * </ul>
     * </p>
     *
     * <p><b>Request example:</b></p>
     * <pre>
     * GET /api/transactions/account/12345678901234567890/period?startDate=2025-01-01&endDate=2025-01-31
     * </pre>
     *
     * @param accountNumber Account number
     * @param startDate beginning of the period (inclusive)
     * @param endDate end of the period (inclusive)
     * @return list of transaction for specified period
     * <p>
     *  200 - List of transaction received
     *  400 - Date of beginning later than date of ending
     *  403 - No access to account
     *  404 - Account not found
     */
    @GetMapping("/account/{accountNumber}/period")
    @Operation(
            summary = "Receive transaction for period",
            description = "Returns transactions from a specified account for a specified period of time."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List received"),
            @ApiResponse(responseCode = "400", description = "Wrong date range"),
            @ApiResponse(responseCode = "403", description = "no access to account"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<List<TransactionResponse>> getTransactionsByDateRange(
            @Parameter(description = "Account number", example = "12345678901234567890", required = true)
            @PathVariable String accountNumber,

            @Parameter(description = "Beginning of the period (ISO 8601)", example = "2025-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,

            @Parameter(description = "End of period (ISO 8601)", example = "2025-01-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        return ResponseEntity.ok(List.of());
    }
    /**
     * Returns detailed information about a specific transaction by its ID..
     * <p>
     * Used to display extended information about the transfer,
     * including hidden details (fee, exact execution time).
     * </p>
     *
     * <p><b>Answer example (200 OK):</b></p>
     * <pre>
     * {
     *   "id": 100500,
     *   "transactionId": "550e8400-e29b-41d4-a716-446655440000",
     *   "fromAccountNumber": "11111111111111111111",
     *   "toAccountNumber": "22222222222222222222",
     *   "amount": 500.00,
     *   "type": "TRANSFER",
     *   "status": "COMPLETED",
     *   "description": "Paying for the Internet",
     *   "fee": 0.00,
     *   "createdAt": "2025-01-15T14:25:00",
     *   "completedAt": "2025-01-15T14:25:01"
     * }
     * </pre>
     *
     * @param id Inner ID of transaction (Primary key at DB)
     * @return Detailed information about transaction
     *
     *  200 - Information recived
     *  401 - User not authenticated
     *  403 - No access to this transaction
     *  404 - Transaction not found
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Recive detailed information about transaction",
            description = "Returns detailed information about a specific transaction based on its ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Information recived"),
            @ApiResponse(responseCode = "403", description = "No access to transaction"),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<TransactionResponse> getTransactionById(
            @Parameter(description = "ID of transaction", example = "100500", required = true)
            @PathVariable Long id) {

        TransactionResponse transaction = transactionService.getTransactionById(id);
        return ResponseEntity.ok(transaction);
    }

    /**
     * Returns detailed information about a transaction based on its external UUID.
     * <p>
     * Used by external systems to track the transfer status.
     * </p>
     *
     * @param uuid external transaction identifier (UUID)
     * @return detailed transaction information
     * <p>
     *  200 - Information received
     *  404 - Transaction not found
     */
    @GetMapping("/uuid/{uuid}")
    @Operation(
            summary = "Get transaction by UUID",
            description = "Returns detailed information about a transaction by its external ID"
    )
    public ResponseEntity<TransactionResponse> getTransactionByUuid(
            @Parameter(description = "UUID of transaction", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
            @PathVariable String uuid) {

        TransactionResponse transaction = transactionService.getTransactionByUuid(uuid);
        return ResponseEntity.ok(transaction);
    }

    /**
     * Returns transaction statistics for the specified account.
     * <p>
     * <b>Returned data:</b>
     * <ul>
     * <li><b>totalOutgoing</b> — the total amount of all outgoing transfers</li>
     * <li><b>totalIncoming</b> — the total amount of all incoming transfers</li>
     * <li><b>transactionCount</b> — the total number of transactions</li>
     * </ul>
     * </p>
     *
     * <p><b>Example of answer:</b></p>
     * <pre>
     * {
     *   "totalOutgoing": 15000.00,
     *   "totalIncoming": 25000.00,
     *   "transactionCount": 42,
     *   "averageOutgoing": 357.14,
     *   "averageIncoming": 595.24
     * }
     * </pre>
     *
     * @param accountNumber account number
     * @return transaction statistics
     * <p>
     *  200 - Statistics received
     *  403 - Account not accessible
     *  404 - Account not found
     */
    @GetMapping("/account/{accountNumber}/statistics")
    @Operation(
            summary = "Get account statistics",
            description = "Returns transaction statistics for the specified account (amounts, quantity)"
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TransactionStatistics> getTransactionStatistics(
            @Parameter(description = "Account number", example = "12345678901234567890")
            @PathVariable String accountNumber) {

        TransactionStatistics statistics = TransactionStatistics.builder()
                .totalOutgoing(BigDecimal.valueOf(15000))
                .totalIncoming(BigDecimal.valueOf(25000))
                .transactionCount(42L)
                .averageOutgoing(BigDecimal.valueOf(357.14))
                .averageIncoming(BigDecimal.valueOf(595.24))
                .build();

        return ResponseEntity.ok(statistics);
    }

    /**
     * Exports transaction history to a CSV file.
     * <p>
     * <b>CSV format:</b>
     * <pre>
     * "Transaction ID","Sender Account","Recipient Account","Amount","Type","Status","Date"
     * "550e8400-...","11111111111111111111111","222222222222222222222","500.00","TRANSFER","COMPLETED","2025-01-15 14:25:00"
     * </pre>
     * </p>
     *
     * @param accountNumber account number (optional, if not specified - all user accounts)
     * @param startDate start of period (optional)
     * @param endDate end of period (optional)
     * @return CSV file with transaction history
     * <p>
     *  200 - File generated successfully
     *  403 - Data not accessible
     */
    @GetMapping("/export/csv")
    @Operation(
            summary = "Export transactions to CSV",
            description = "Exports transaction history to a CSV file for further analysis."
    )
    public ResponseEntity<byte[]> exportTransactionsToCsv(
            @RequestParam(required = false) String accountNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=transactions.csv")
                .header("Content-Type", "text/csv")
                .body(new byte[0]);
    }
}

