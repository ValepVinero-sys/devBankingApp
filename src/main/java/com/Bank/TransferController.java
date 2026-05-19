package com.Bank;

import com.Bank.TransferRequest;
import com.Bank.TransactionResponse;
import com.Bank.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing bank transfers.
 * <p>
 * Provides an API for making transfers between user accounts.
 * </p>
 *
 * @author Valep Vinreo
 * @version 1.0
 * @since 04-2026
 */
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Transfer's", description = "API for funds transfers")
public class TransferController {

    private final TransferService transferService;

    /**
     * Performs transfer of funds between accounts.
     *
     * @param request object with translation data (cannot be null)
     * @return {@link ResponseEntity} with object {@link TransactionResponse}
     *
     * @apiNote Requires user authentication.
     *          The transfer amount cannot exceed the available balance..
     */
    @PostMapping("/transfer")
    @Operation(
            summary = "Funds transfer",
            description = "Transfer funds between account"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transfer successfully"),
            @ApiResponse(responseCode = "400", description = "Wrong request param (Not enough funds, wrong account & etc.)"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Sender account or receiver not found")
    })
    public ResponseEntity<TransactionResponse> transfer(
            @Parameter(description = "Transfer data", required = true)
            @Valid @RequestBody TransferRequest request) {

        TransactionResponse response = transferService.transferMoney(request);
        return ResponseEntity.ok(response);
    }
}
