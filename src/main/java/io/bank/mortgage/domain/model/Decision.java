package io.bank.mortgage.domain.model;

import io.bank.mortgage.datatype.DecisionType;
import io.bank.mortgage.datatype.Status;
import jdk.jshell.Snippet;
import lombok.*;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("decisions")
public class Decision {

    @Id
    private UUID id;

    @Column("user_id")
    private String userId;

    @Column("public_id")
    private String publicId;

    private DecisionType decisionType;

    @Column("officer_user_id")
    private String officerUserId;

    @Column("officer_name")
    private String officerName;

    private String comments;

    @CreatedDate
    @Column("created_at")
    private String createdAt;

    @LastModifiedDate
    @Column("decided_at")
    private String decidedAt;


    public void onCreate() {
        this.publicId = UUID.randomUUID().toString();
        this.createdAt = String.valueOf(OffsetDateTime.from(Instant.now()));
    }

    public void onUpdate() {
        this.decidedAt = String.valueOf(OffsetDateTime.from(Instant.now()));
    }
}