package io.bank.mortgage.dto;

import io.bank.mortgage.datatype.DecisionType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DecisionRequest {
    @NotBlank
    private DecisionType decision;
    private String comments;
}
