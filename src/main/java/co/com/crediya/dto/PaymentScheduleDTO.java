package co.com.crediya.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PaymentScheduleDTO {

    private int month;
    private BigDecimal capital;
    private BigDecimal interest;
    private BigDecimal remainingBalance;

    @Override
    public String toString() {
        return "PaymentScheduleDTO{" +
                " Mes =" + month +
                ", Capital =" + capital +
                ", Intereses =" + interest +
                ", Saldo restante =" + remainingBalance +
                '}';
    }
}
