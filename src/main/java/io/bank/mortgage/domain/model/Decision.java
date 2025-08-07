package io.bank.mortgage.domain.model;

import io.bank.mortgage.datatype.DecisionType;
import io.bank.mortgage.datatype.Status;
import jdk.jshell.Snippet;
import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("decisions")
public class Decision {

    @Id
    private UUID id;

    @Column("application_id")
    private UUID applicationId;

    private DecisionType decisionType;

    @Column("officer_user_id")
    private String officerUserId;

    @Column("officer_name")
    private String officerName;

    private String comments;

    @CreatedDate
    @Column("decided_at")
    private Instant decidedAt;
}