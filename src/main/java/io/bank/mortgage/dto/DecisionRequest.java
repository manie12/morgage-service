package io.bank.mortgage.dto;

import lombok.Data;

@Data
public class DecisionRequest {
    @NotBlank
    private String decision;
    private String comments;
}
