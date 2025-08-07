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
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("documents")
public class Document {

    @Id
    private UUID id;

    @Column("application_id")
    private UUID applicationId;

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
    private Instant createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private Instant updatedAt;
}