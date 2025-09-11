package co.com.crediya.entrypoints;
import co.com.crediya.drivenadapters.SQSPublisherAdapter;
import co.com.crediya.drivenadapters.config.AwsClientFactory;
import co.com.crediya.drivenadapters.port.out.ResultEventPublisher;
import co.com.crediya.dto.CalculationRequestDTO;
import co.com.crediya.dto.LoanEvaluationResultEvent;
import co.com.crediya.usecase.CalculateDebtUseCase;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.sqs.SqsClient;


public class Handler implements RequestHandler<SQSEvent, String> {

    private final SqsClient sqsClient;
    private final String queueNameResult;

    private final ObjectMapper mapper = new ObjectMapper();
    private final CalculateDebtUseCase useCase = new CalculateDebtUseCase();
    private final ResultEventPublisher eventPublisher;

    public Handler() {
        this(AwsClientFactory.createSqsClient(), System.getenv("AWS_QUEUE_DEBT_RESULT"));
    }

    public Handler(SqsClient sqsClient, String queueNameResult) {
        this.sqsClient = sqsClient;
        this.queueNameResult = queueNameResult;
        this.eventPublisher = new SQSPublisherAdapter(sqsClient, mapper, queueNameResult);
    }

    @Override
    public String handleRequest(SQSEvent sqsEvent, Context context) {

        for (SQSEvent.SQSMessage message : sqsEvent.getRecords()) {
            try {
                CalculationRequestDTO dto = mapper.readValue(message.getBody(), CalculationRequestDTO.class);
                LoanEvaluationResultEvent resultEvent = useCase.execute(dto);
                eventPublisher.publish(resultEvent);
                context.getLogger().log("Mensaje procesado para el usuario: " + dto.getDocumentNumber() + ", estado: "+resultEvent.getDecision());
            } catch (Exception e) {
                context.getLogger().log("Error procesando mensaje SQS: " + message.getBody() + " Error: " + e.getMessage());
                // RuntimeException in order that message stay to retry
            }
        }
        return "Procesados " + sqsEvent.getRecords().size() + " mensajes.";
    }

}
