package com.Bank;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
/**
 * DTO for statistic of transaction of all accounts.
 *
 * @author Valep Vinreo
 * @version 1.0
 * @since 04-2026
 */
@Data
@Builder
public class TransactionStatistics {

    /** Overall sum of outgoing transfer */
    private BigDecimal totalOutgoing;

    /** Overall sum of incoming transfer */
    private BigDecimal totalIncoming;

    /** Overall transaction count */
    private Long transactionCount;

    /** Average sum of outgoing transfer */
    private BigDecimal averageOutgoing;

    /** Average sum of incoming transfer */
    private BigDecimal averageIncoming;
}
