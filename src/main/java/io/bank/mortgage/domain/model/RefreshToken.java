package io.bank.mortgage.domain.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("refresh_tokens")
public class RefreshToken {

    @Id
    private UUID token;            // primary key

    @Column("user_id")
    private UUID userId;

    @Column("expires_at")
    private OffsetDateTime expiresAt;

    private Boolean revoked;

    @Column("created_at")
    private OffsetDateTime createdAt;
}
