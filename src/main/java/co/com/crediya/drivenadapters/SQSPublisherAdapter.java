package co.com.crediya.drivenadapters;

import co.com.crediya.drivenadapters.port.out.ResultEventPublisher;
import co.com.crediya.dto.LoanEvaluationResultEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

public class SQSPublisherAdapter implements ResultEventPublisher {

    private final SqsClient sqsClient;
    private final ObjectMapper mapper;
    private final String queueUrl;

    public SQSPublisherAdapter(SqsClient sqsClient, ObjectMapper mapper, String queueUrl) {
        this.sqsClient = sqsClient;
        this.mapper = mapper;
        this.queueUrl = queueUrl;
    }


    @Override
    public void publish(LoanEvaluationResultEvent event) {
        try {
            String message = mapper.writeValueAsString(event);

            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(message)
                    .build();

            sqsClient.sendMessage(request);
        } catch (Exception e) {
            throw new RuntimeException("Error publishing event to SQS", e);
        }
    }
}
