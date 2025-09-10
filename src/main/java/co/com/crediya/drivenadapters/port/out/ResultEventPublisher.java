package co.com.crediya.drivenadapters.port.out;

import co.com.crediya.dto.LoanEvaluationResultEvent;

public interface ResultEventPublisher {
    void publish(LoanEvaluationResultEvent event);
}
