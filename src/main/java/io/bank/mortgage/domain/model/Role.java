package io.bank.mortgage.domain.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("roles")
public class Role {
    @Id
    private Integer id;
    private String name;           // APPLICANT, OFFICER
    private String description;    // Applicant, Officer
    @Column("user_id")
    private String userId;

    @CreatedDate
    @Column("created_at")
    private String createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private String updatedAt;

    public void onCreate() {
        this.createdAt = String.valueOf(OffsetDateTime.from(Instant.now()));
    }

    public void onUpdate() {
        this.updatedAt = String.valueOf(OffsetDateTime.from(Instant.now()));
    }
}