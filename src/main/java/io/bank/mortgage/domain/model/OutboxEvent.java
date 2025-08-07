package io.bank.mortgage.domain.model;


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
@Table("outbox_events")
public class OutboxEvent {

    @Id
    private UUID id;

    @Column("aggregate_type")
    private String aggregateType;

    @Column("aggregate_id")
    private UUID aggregateId;

    @Column("event_type")
    private String eventType;

    /**
     * Store as JSON String (or JsonNode via converter)
     */
    private String payload;

    private String headers;

    @CreatedDate
    @Column("occurred_at")
    private Instant occurredAt;

    @Column("published_at")
    private Instant publishedAt;

    private Integer attempts;
}