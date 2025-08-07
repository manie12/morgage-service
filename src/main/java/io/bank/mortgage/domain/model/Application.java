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
@Table("applications")
public class Application {

    @Id
    private UUID id;


    @Version
    private Integer version;

    @Column("external_ref")
    private String externalRef;

    @Column("applicant_user_id")
    private String applicantUserId;

    @Column("national_id_hash")
    private String nationalIdHash;

    @Column("national_id_enc")
    private byte[] nationalIdEnc;

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
    private Instant createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private Instant updatedAt;

    @Column("submitted_at")
    private Instant submittedAt;

    @Column("decided_at")
    private Instant decidedAt;

    @Column("soft_deleted")
    private Boolean softDeleted;
}