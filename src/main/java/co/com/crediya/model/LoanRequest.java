package co.com.crediya.model;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoanRequest {
    private int id;
    private String documentNumber;
    private LoanType loanType;
    private BigDecimal amount;
    private int termMonths;
}
