package co.com.crediya;


import co.com.crediya.drivenadapters.SQSPublisherAdapter;
import co.com.crediya.dto.ActiveLoanDTO;
import co.com.crediya.dto.CalculationRequestDTO;
import co.com.crediya.dto.LoanEvaluationResultEvent;
import co.com.crediya.usecase.CalculateDebtUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MainApp {
    public static void main(String[] args) {

        CalculateDebtUseCase calculateDebtUseCase = new CalculateDebtUseCase();
        CalculationRequestDTO dto = new CalculationRequestDTO();

        List<ActiveLoanDTO> activeLoans = new ArrayList<>();
        ActiveLoanDTO activeLoanDTO = new ActiveLoanDTO();
        activeLoanDTO.setAmount(BigDecimal.valueOf(1000000));
        activeLoanDTO.setTermMonths(12);
        activeLoanDTO.setInterestRate(new BigDecimal("0.0450"));
        activeLoans.add(activeLoanDTO);

        dto.setDocumentNumber("1122");
        dto.setLoanRequestId("27");
        dto.setAmount(BigDecimal.valueOf(50000));
        dto.setBaseSalary(BigDecimal.valueOf(8000000));
        dto.setTermMonths(24);
        dto.setInterestRate(new BigDecimal("0.045"));
        dto.setActiveLoans(activeLoans);

        LoanEvaluationResultEvent response = calculateDebtUseCase.execute(dto);
        System.out.println(response.getDecision());
        System.out.println(response.getMonthlyInstallment());
        System.out.println(response.getPaymentPlan());

        SqsClient sqsClient = SqsClient.builder()
                .region(Region.US_EAST_1)
                .build();

        String queueUrl = "https://sqs.us-east-1.amazonaws.com/791077912250/credit-decision-response";

        SQSPublisherAdapter publisher = new SQSPublisherAdapter(sqsClient, new ObjectMapper(), queueUrl);
        publisher.publish(response);
        System.out.println("Mensaje publicado con decision: " + response.getDecision());
    }
}
