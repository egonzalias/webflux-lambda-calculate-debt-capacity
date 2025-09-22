package co.com.crediya.usecase;

import co.com.crediya.dto.ActiveLoanDTO;
import co.com.crediya.dto.CalculationRequestDTO;
import co.com.crediya.dto.LoanEvaluationResultEvent;
import co.com.crediya.enums.LoanStatusEnum;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CalculateDebtUseCaseTest {

    private final CalculateDebtUseCase useCase = new CalculateDebtUseCase();

    private CalculationRequestDTO buildRequest(BigDecimal baseSalary, BigDecimal requestedAmount,
                                               BigDecimal interestRate, int termMonths,
                                               List<ActiveLoanDTO> activeLoans, Long loanRequestId) {
        CalculationRequestDTO dto = new CalculationRequestDTO();
        dto.setBaseSalary(baseSalary);
        dto.setAmount(requestedAmount);
        dto.setInterestRate(interestRate);
        dto.setTermMonths(termMonths);
        dto.setActiveLoans(activeLoans);
        dto.setLoanRequestId(String.valueOf(loanRequestId));
        return dto;
    }

    @Test
    void testLoanRejectedWhenInstallmentExceedsCapacity() {
        BigDecimal baseSalary = new BigDecimal("1000");
        BigDecimal requestedAmount = new BigDecimal("10000");
        BigDecimal interestRate = new BigDecimal("0.12");
        int termMonths = 12;

        // Active loans with big installments to reduce available debt capacity
        ActiveLoanDTO activeLoan = new ActiveLoanDTO(1L, new BigDecimal("10000"), 12, new BigDecimal("0.1"));
        List<ActiveLoanDTO> activeLoans = List.of(activeLoan);

        CalculationRequestDTO request = buildRequest(baseSalary, requestedAmount, interestRate, termMonths, activeLoans, 1L);

        LoanEvaluationResultEvent result = useCase.execute(request);

        assertEquals(LoanStatusEnum.RECH.name(), result.getDecision());
        assertTrue(result.getPaymentPlan().isEmpty(), "Payment plan should be empty on rejection");
    }

    @Test
    void testLoanRejectedWhenQuotaExceedsCapacityDespiteAmountBeingHigh() {
        BigDecimal baseSalary = new BigDecimal("1000");
        BigDecimal requestedAmount = baseSalary.multiply(BigDecimal.valueOf(6)); // More than 5x salary
        BigDecimal interestRate = new BigDecimal("0.12");
        int termMonths = 12;

        List<ActiveLoanDTO> activeLoans = List.of(
                new ActiveLoanDTO(1l, new BigDecimal("3000"),  12, interestRate)
        );

        CalculationRequestDTO request = buildRequest(baseSalary, requestedAmount, interestRate, termMonths, activeLoans, 2L);

        LoanEvaluationResultEvent result = useCase.execute(request);

        assertEquals(LoanStatusEnum.RECH.name(), result.getDecision());
        assertTrue(result.getPaymentPlan().isEmpty(), "Payment plan should be empty when loan is rejected");
    }

    @Test
    void testLoanApproved() {
        BigDecimal baseSalary = new BigDecimal("1000");
        BigDecimal requestedAmount = new BigDecimal("2000");
        BigDecimal interestRate = new BigDecimal("0.12");
        int termMonths = 12;

        List<ActiveLoanDTO> activeLoans = List.of();

        CalculationRequestDTO request = buildRequest(baseSalary, requestedAmount, interestRate, termMonths, activeLoans, 3L);

        LoanEvaluationResultEvent result = useCase.execute(request);

        assertEquals(LoanStatusEnum.APROB.name(), result.getDecision());
        assertFalse(result.getPaymentPlan().isEmpty(), "Payment plan should not be empty when approved");

        // Check monthly installment rounded scale
        assertEquals(2, result.getMonthlyInstallment().scale());

        // Validate available debt capacity is positive
        assertTrue(result.getAvailableCapacity().compareTo(BigDecimal.ZERO) >= 0);
    }
}
