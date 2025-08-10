package io.bank.mortgage.domain.model;

import io.bank.mortgage.datatype.DocumentStatus;
import io.bank.mortgage.datatype.Status;
import jdk.jshell.Snippet;
import lombok.*;
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
@Table("documents")
public class Document {

    @Id
    private UUID id;

    @Column("user_id")
    private String userId;

    @Column("public_id")
    private String publicId;

    private String type;

    @Column("file_name")
    private String fileName;

    @Column("content_type")
    private String contentType;

    @Column("size_bytes")
    private Long sizeBytes;

    @Column("upload_url")
    private String uploadUrl;

    private String checksum;

    private DocumentStatus documentStatus;

    @CreatedDate
    @Column("created_at")
    private String createdAt;

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