package co.com.crediya.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ActiveLoanDTO {
    private Long id;
    private BigDecimal amount;
    private int termMonths;
    private BigDecimal interestRate;
}
