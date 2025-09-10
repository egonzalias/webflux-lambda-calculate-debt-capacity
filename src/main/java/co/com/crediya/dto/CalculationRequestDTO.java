package co.com.crediya.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CalculationRequestDTO {
    private String documentNumber;
    private int loanRequestId;
    private BigDecimal amount;
    private BigDecimal baseSalary;
    private int termMonths;
    private BigDecimal interestRate; // annual
    private List<ActiveLoanDTO> activeLoans;
}
