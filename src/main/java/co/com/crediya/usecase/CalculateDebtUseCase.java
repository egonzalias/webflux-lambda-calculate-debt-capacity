package co.com.crediya.usecase;

import co.com.crediya.dto.ActiveLoanDTO;
import co.com.crediya.dto.CalculationRequestDTO;
import co.com.crediya.dto.LoanEvaluationResultEvent;
import co.com.crediya.dto.PaymentScheduleDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class CalculateDebtUseCase {

    public LoanEvaluationResultEvent execute(CalculationRequestDTO calculationRequestDTO) {

        BigDecimal baseSalary = calculationRequestDTO.getBaseSalary();
        BigDecimal requestedAmount  = calculationRequestDTO.getAmount();
        BigDecimal annualInterestRate = calculationRequestDTO.getInterestRate();
        int requestedTermMonths = calculationRequestDTO.getTermMonths();

        // Calculate max allowed debt (35% of base salary)
        BigDecimal maxAllowedDebt = baseSalary.multiply(BigDecimal.valueOf(0.35));

        // Calculate total monthly debt from active loans
        BigDecimal currentMonthlyDebt = calculateTotalActiveLoanInstallments(calculationRequestDTO.getActiveLoans());

        // Calculate available debt capacity
        BigDecimal availableDebtCapacity = maxAllowedDebt.subtract(currentMonthlyDebt);

        // Calculate monthly installment for the new loan
        BigDecimal newLoanMonthlyInstallment = calculateMonthlyInstallment(requestedAmount, annualInterestRate, requestedTermMonths);

        // Generate payment plan
        List<PaymentScheduleDTO> paymentPlan = generatePaymentPlan(
                requestedAmount, annualInterestRate, requestedTermMonths
        );

        String decision;
        if (newLoanMonthlyInstallment.compareTo(availableDebtCapacity) > 0) {
            decision = "RECHAZADO";
            paymentPlan.clear();
        } else if (requestedAmount.compareTo(baseSalary.multiply(BigDecimal.valueOf(5))) > 0) {
            decision = "REVISION_MANUAL";
        } else {
            decision = "APROBADO";
        }

        return new LoanEvaluationResultEvent(
                decision,
                newLoanMonthlyInstallment.setScale(2, RoundingMode.HALF_UP),
                availableDebtCapacity.setScale(2, RoundingMode.HALF_UP),
                maxAllowedDebt.setScale(2, RoundingMode.HALF_UP),
                currentMonthlyDebt.setScale(2, RoundingMode.HALF_UP),
                paymentPlan
        );
    }

    /**
     * Calculates the monthly installment for a loan using the amortization formula.
     */
    private BigDecimal calculateMonthlyInstallment(BigDecimal principal, BigDecimal annualRate, int months) {
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
        BigDecimal onePlusRatePower = monthlyRate.add(BigDecimal.ONE).pow(months);
        return principal
                .multiply(monthlyRate)
                .multiply(onePlusRatePower)
                .divide(onePlusRatePower.subtract(BigDecimal.ONE), 10, RoundingMode.HALF_UP);
    }

    /**
     * Sums up the monthly installments of all active loans.
     */
    private BigDecimal calculateTotalActiveLoanInstallments(List<ActiveLoanDTO> loans) {
        return loans.stream()
                .map(loan -> calculateMonthlyInstallment(
                        loan.getAmount(),
                        loan.getInterestRate(), //Annual
                        loan.getTermMonths())
                ).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<PaymentScheduleDTO> generatePaymentPlan(BigDecimal amount, BigDecimal annualRate, int termMonths) {
        List<PaymentScheduleDTO> schedule = new ArrayList<>();

        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
        BigDecimal onePlusRatePower = monthlyRate.add(BigDecimal.ONE).pow(termMonths);

        BigDecimal monthlyInstallment = amount
                .multiply(monthlyRate)
                .multiply(onePlusRatePower)
                .divide(onePlusRatePower.subtract(BigDecimal.ONE), 10, RoundingMode.HALF_UP);

        BigDecimal remainingBalance = amount;

        for (int month = 1; month <= termMonths; month++) {
            // Interest for current balance
            BigDecimal interestPayment = remainingBalance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);

            // Capital = cuota - interés
            BigDecimal capitalPayment = monthlyInstallment.subtract(interestPayment).setScale(2, RoundingMode.HALF_UP);

            // Update remaining balance
            remainingBalance = remainingBalance.subtract(capitalPayment).setScale(2, RoundingMode.HALF_UP);

            // Prevent negative balance in last payment
            if (month == termMonths && remainingBalance.compareTo(BigDecimal.ZERO) < 0) {
                remainingBalance = BigDecimal.ZERO;
            }

            schedule.add(new PaymentScheduleDTO(month, capitalPayment, interestPayment, remainingBalance));
        }

        return schedule;
    }
}
