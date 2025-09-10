package co.com.crediya.model;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class User {
    private String documentNumber;
    private String firstName;
    private String lastName;
    private String email;
    private BigDecimal baseSalary;
    private String role;
}
