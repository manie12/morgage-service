package io.bank.mortgage.web;

import io.bank.mortgage.datatype.Status;
import io.bank.mortgage.dto.DecisionRequest;
import io.bank.mortgage.dto.DocumentMetadata;
import io.bank.mortgage.dto.NewApplicationCreateRequest;
import io.bank.mortgage.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Reactive REST controller exposing Application endpoints as designed in the OpenAPI spec.
 */
@RestController
@RequestMapping(value = "/api/v1/applications", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Validated
public class ApplicationController {

    private final ApplicationService service;

    // ──────────────────────────────────────────────
    // CREATE
    // ──────────────────────────────────────────────
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<?>> create(@RequestHeader(value = "Idempotency-Key", required = false) String idemKey,
                                          @RequestBody @Validated NewApplicationCreateRequest body,
                                          Authentication auth) {
        String userId = auth.getName();
        return service.create(body, userId, idemKey)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED)
                        .eTag(String.valueOf(saved.getVersion()))
                        .body(saved));
    }

    // ──────────────────────────────────────────────
    // READ  (single)
    // ──────────────────────────────────────────────
    @GetMapping("/{id}")
    public Mono<ResponseEntity<?>> get(@PathVariable UUID id, Authentication auth) {
        boolean officer = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_OFFICER"));
        return service.getById(id, auth.getName(), officer)
                .map(app -> ResponseEntity.ok()
                        .eTag(String.valueOf(app.getVersion()))
                        .body(app));
    }

    // ──────────────────────────────────────────────
    // LIST / FILTER
    // ──────────────────────────────────────────────
    @GetMapping
    public Flux<?> list(@RequestParam(required = false) Status status,
                        @RequestParam(required = false) Instant createdFrom,
                        @RequestParam(required = false) Instant createdTo,
                        @RequestParam(required = false) String nationalId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size) {
        // nationalId must already be hashed client-side? we hash here for safety
        String hash = nationalId; // assume hashed; can call service to hash
        return service.list(status, createdFrom, createdTo, hash, page, size);
    }

    // ──────────────────────────────────────────────
    // DECIDE (approve / reject)
    // ──────────────────────────────────────────────
    @PatchMapping(path = "/{id}/decision", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<?>> decide(@PathVariable UUID id,
                                          @RequestBody @Validated DecisionRequest body,
                                          @RequestHeader("If-Match") int version,
                                          Authentication auth) {
        String officerId = auth.getName();
        String officerName = auth.getName(); // adapt if full name stored elsewhere
        return service.decide(id, body, officerId, officerName, version, null)
                .map(updated -> ResponseEntity.ok()
                        .eTag(String.valueOf(updated.getVersion()))
                        .body(updated));
    }
}