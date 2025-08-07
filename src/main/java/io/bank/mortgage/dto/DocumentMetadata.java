package io.bank.mortgage.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DocumentMetadata {

    @NotBlank
    private String type;          // e.g. NATIONAL_ID, PAYSLIP

    @NotBlank
    private String fileName;      // original filename

    @NotBlank
    private String contentType;   // MIME type

    @NotNull
    @Min(1)
    private Long sizeBytes;

    @NotBlank
    private String uploadUrl;     // pre‑signed URL to S3‑style store

    private String checksum;      // optional SHA‑256
}
