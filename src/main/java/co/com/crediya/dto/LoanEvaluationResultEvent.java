package co.com.crediya.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoanEvaluationResultEvent {
    private String idLoanRequest;
    private String decision;
    private BigDecimal monthlyInstallment;
    private BigDecimal availableCapacity;
    private BigDecimal maxCapacity;
    private BigDecimal currentDebt;
    private List<PaymentScheduleDTO> paymentPlan;
}
