package co.com.crediya;

import co.com.crediya.dto.MessageBodyDTO;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sns.SnsAsyncClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HandlerTest {


    private Handler handler;
    @Mock
    private SnsAsyncClient sns;
    @Mock
    private Context context;
    @Captor
    private ArgumentCaptor<PublishRequest> requestCaptor;

    @BeforeEach
    void setup() {
        handler = new Handler(sns, "arn:aws:sns:us-east-1:123456789012:test-topic");
    }

    private SQSEvent generateSqsEvent(String body) {
        SQSEvent event = new SQSEvent();
        SQSEvent.SQSMessage msg = new SQSEvent.SQSMessage();
        msg.setBody(body);
        event.setRecords(List.of(msg));
        return event;
    }

    @Test
    void handleRequest_shouldPublishMessageSuccessfully() {
        // Arrange
        MessageBodyDTO dto = new MessageBodyDTO("123", "Aprobado", "Juan Pérez", "juan@email.com");
        String json = "{\"idLoanRequest\":\"123\",\"status\":\"Aprobado\",\"fullName\":\"Juan Pérez\",\"email\":\"juan@email.com\"}";

        SQSEvent event = generateSqsEvent(json);

        PublishResponse response = PublishResponse.builder().messageId("msg-1").build();
        CompletableFuture<PublishResponse> future = CompletableFuture.completedFuture(response);

        when(sns.publish(any(PublishRequest.class))).thenReturn(future);

        String result = handler.handleRequest(event, context);

        assertEquals("Mensajes publicados con éxito", result);
        verify(sns).publish(requestCaptor.capture());
        assertTrue(requestCaptor.getValue().message().contains("Hola Juan Pérez"));
    }

    @Test
    void handleRequest_shouldFailOnInvalidJson() {
        // Arrange
        SQSEvent event = generateSqsEvent("{INVALID_JSON");

        // Act + Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            handler.handleRequest(event, context);
        });

        Throwable cause = exception.getCause(); // CompletionException
        assertNotNull(cause);
        assertTrue(cause instanceof CompletionException);

        Throwable innerCause = cause.getCause(); // JsonProcessingException
        assertNotNull(innerCause);
        assertTrue(innerCause instanceof JsonProcessingException);
    }

    @Test
    void handleRequest_shouldFailIfSnsPublishFails() {
        // Arrange
        MessageBodyDTO dto = new MessageBodyDTO("123", "Rechazado", "Maria Gomez", "maria@email.com");
        String json = "{\"idLoanRequest\":\"123\",\"status\":\"Rechazado\",\"fullName\":\"Maria Gomez\",\"email\":\"maria@email.com\"}";

        SQSEvent event = generateSqsEvent(json);

        CompletableFuture<PublishResponse> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("SNS publish failed"));

        when(sns.publish(any(PublishRequest.class))).thenReturn(failedFuture);

        // Act + Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            handler.handleRequest(event, context);
        });

        assertTrue(exception.getMessage().contains("SNS publish failed"));
    }

}
