package io.bank.mortgage.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("users")
public class User {

    @Id
    private Long id;
    @Column("public_id")
    private String publicId;
    @Column("national_id")
    private String nationalId;
    @Column("password_hash")
    private String passwordHash;

    /**
     * Roles are mapped via the user_roles join table.  This collection is
     * populated manually or via a custom query; it’s marked @Transient so
     * Spring Data R2DBC doesn’t expect a column named "roles".
     */
    @Transient
    @Builder.Default
    private Set<String> roles = Set.of();

    @Column("created_at")
    private String createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private String updatedAt;

    @Column("created_by")
    private String createdBy;

    public void onCreate(String userId) {
        this.publicId = UUID.randomUUID().toString();
        this.createdAt = String.valueOf(OffsetDateTime.from(Instant.now()));
    }

    public void onUpdate() {
        this.updatedAt = String.valueOf(OffsetDateTime.from(Instant.now()));
    }
}