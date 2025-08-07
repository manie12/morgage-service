package io.bank.mortgage.dto;


import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class NewApplicationCreateRequest {
    private String externalRef;
    @NotBlank
    private String nationalId;
    @NotBlank
    private String fullName;
    @Email
    private String email;
    @NotBlank
    private String phone;
    @Positive
    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal loanAmount;
    @NotBlank
    private String currency;
    @PositiveOrZero
    private BigDecimal income;
    @PositiveOrZero
    private BigDecimal liabilities;
    @NotBlank
    private String propertyAddress;
    @PositiveOrZero
    private BigDecimal propertyValue;
    @NotBlank
    String propertyType;
    private List<DocumentMetadata> documents;

}
