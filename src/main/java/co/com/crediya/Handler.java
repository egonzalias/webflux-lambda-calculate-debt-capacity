package co.com.crediya;
import co.com.crediya.dto.MessageBodyDTO;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.sns.SnsAsyncClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Handler implements RequestHandler<SQSEvent, String> {

    private final SnsAsyncClient sns;
    private final String topicArn;
    private final ObjectMapper mapper = new ObjectMapper();

    public Handler() {
        this(SnsAsyncClient.create(), System.getenv("AWS_SNS_TOPIC_ARN"));
    }
    public Handler(SnsAsyncClient sns, String topicArn) {
        this.sns = sns;
        this.topicArn = topicArn;
    }

    @Override
    public String handleRequest(SQSEvent sqsEvent, Context context) {
        // Convert each SQS message into a CompletableFuture for SNS publish
        List<CompletableFuture<PublishResponse>> futures = sqsEvent.getRecords().stream()
                .map(record -> {
                    try {
                        MessageBodyDTO msg = mapper.readValue(record.getBody(), MessageBodyDTO.class);
                        return processMessage(msg);
                    } catch (Exception e) {
                        CompletableFuture<PublishResponse> failedFuture = new CompletableFuture<>();
                        failedFuture.completeExceptionally(e);
                        return failedFuture;
                    }
                }).toList();

        // Wait for all async publish operations to complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        try {
            allFutures.join(); // Block until all SNS publishes complete
            return "Mensajes publicados con éxito";
        } catch (Exception e) {
            context.getLogger().log("Error publicando mensajes: " + e.getMessage());
            throw new RuntimeException("Error publicando mensajes", e);
        }
    }

    // Publish message to SNS asynchronously
    public CompletableFuture<PublishResponse> processMessage(MessageBodyDTO msg) {
        String message = String.format(
                "Hola %s, te informamos que la solicitud de préstamo Nro %s ha sido %s",
                msg.fullName(), msg.idLoanRequest(), msg.status());

        PublishRequest request = PublishRequest.builder()
                .topicArn(topicArn)
                .message(message)
                .subject("Decisión de Solicitud de Préstamo")
                .build();

        // Send message asynchronously and log result
        return sns.publish(request)
                .whenComplete((response, error) -> {
                    if (error != null) {
                        System.err.println("Error publicando en SNS: " + error.getMessage());
                    } else {
                        System.out.println("Mensaje publicado. ID: " + response.messageId());
                    }
                });
    }
}
