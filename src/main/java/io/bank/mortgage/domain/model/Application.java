package io.bank.mortgage.domain.model;

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
@Table("applications")
public class Application {

    @Id
    private UUID id;


    @Version
    private Integer version;

    @Column("external_ref")
    private String externalRef;

    @Column("user_id")
    private String userId;
    @Column("public_id")
    private String publicId;

    @Column("national_id_hash")
    private String nationalIdHash;

    @Column("national_id_enc")
    private String nationalIdEnc;

    @Column("loan_amount")
    private BigDecimal loanAmount;

    @Column("currency")
    private String currency;

    private BigDecimal income;
    private BigDecimal liabilities;

    @Column("property_address")
    private String propertyAddress;

    @Column("property_value")
    private BigDecimal propertyValue;

    @Column("property_type")
    private String propertyType;

    private Status status;

    @CreatedDate
    @Column("created_at")
    private String createdAt;

    @Column("soft_deleted")
    private Boolean softDeleted;

    @LastModifiedDate
    @Column("updated_at")
    private String updatedAt;


    public void onCreate() {
        this.publicId = UUID.randomUUID().toString();
        this.createdAt = String.valueOf(OffsetDateTime.from(Instant.now()));
    }

    public void onUpdate() {
        this.updatedAt = String.valueOf(OffsetDateTime.from(Instant.now()));
    }
}