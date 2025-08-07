package io.bank.mortgage.service.impl;

import io.bank.mortgage.datatype.DecisionType;
import io.bank.mortgage.datatype.DocumentStatus;
import io.bank.mortgage.datatype.EventType;
import io.bank.mortgage.datatype.Status;
import io.bank.mortgage.domain.model.*;
import io.bank.mortgage.dto.DecisionRequest;
import io.bank.mortgage.dto.DocumentMetadata;
import io.bank.mortgage.dto.NewApplicationCreateRequest;
import io.bank.mortgage.exception.VersionConflictException;
import io.bank.mortgage.messaging.MessagingService;
import io.bank.mortgage.repo.*;
import io.bank.mortgage.service.ApplicationService;
import io.bank.mortgage.util.NationalIdService;
import io.bank.mortgage.util.SharedUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository appRepo;
    private final DocumentRepository docRepo;
    private final DecisionRepository decisionRepo;
    private final OutboxEventRepository outboxRepo;
    private final SharedUtils sharedUtils;
    private final NationalIdService nidService;
    private final MessagingService messagingService;
    // ===== CREATE ==========================================================

    @Transactional
    @Override
    public Mono<Application> create(NewApplicationCreateRequest req,
                                    String applicantUserId,
                                    String correlationId) {
        // 1) Idempotency check
        return appRepo.findFirstByApplicantUserIdAndExternalRef(applicantUserId, req.getExternalRef())
                .switchIfEmpty(Mono.defer(() -> doCreate(req, applicantUserId, correlationId)));
    }

    private Mono<Application> doCreate(NewApplicationCreateRequest req, String applicantUserId, String correlationId) {
        Application app = Application.builder()
                .id(UUID.randomUUID())
                .applicantUserId(applicantUserId)
                .externalRef(req.getExternalRef())
                .loanAmount(req.getLoanAmount())
                .currency(req.getCurrency())
                .income(sharedUtils.nvl(req.getIncome()))
                .liabilities(sharedUtils.nvl(req.getLiabilities()))
                .propertyAddress(req.getPropertyAddress())
                .propertyValue(sharedUtils.nvl(req.getPropertyValue()))
                .propertyType(req.getPropertyType())
                .status(Status.SUBMITTED)
                .nationalIdHash(nidService.hash(req.getNationalId()))
                .nationalIdEnc(nidService.encrypt(req.getNationalId()))
                .submittedAt(Instant.now())
                .softDeleted(false)
                .build();

        return appRepo.insert(app)
                .flatMap(saved -> persistDocs(req.getDocuments(), saved.getId())
                        .collectList()
                        .thenReturn(saved))
                .flatMap(saved -> messagingService.publishApplicationEvent(app, EventType.APPLICATION_CREATED, correlationId)
                        .thenReturn(app))
                .doOnSuccess(saved -> log.info("Application {} created", saved.getId()));
    }

    private Flux<Document> persistDocs(List<DocumentMetadata> docs, UUID appId) {
        if (docs == null || docs.isEmpty()) return Flux.empty();
        return Flux.fromIterable(docs)
                .map(meta -> Document.builder()
                        .id(UUID.randomUUID())
                        .applicationId(appId)
                        .type(meta.getType())
                        .fileName(meta.getFileName())
                        .contentType(meta.getContentType())
                        .sizeBytes(meta.getSizeBytes())
                        .uploadUrl(meta.getUploadUrl())
                        .checksum(meta.getChecksum())
                        .documentStatus(DocumentStatus.PENDING_UPLOAD)
                        .build())
                .flatMap(docRepo::insert);
    }

    // ===== READ ============================================================

    @Override
    public Mono<Application> getById(UUID id, String requesterId, boolean officer) {
        return appRepo.findById(id)
                .switchIfEmpty(Mono.error(new ChangeSetPersister.NotFoundException()))
                .flatMap(app -> {
                    if (!officer && !app.getApplicantUserId().equals(requesterId))
                        return Mono.error(new AccessDeniedException("User not authorized to access this application"));
                    return Mono.just(app);
                });
    }

    @Override
    public Flux<Application> list(Status status,
                                  Instant from,
                                  Instant to,
                                  String nationalIdHash,
                                  int page,
                                  int size) {
        return appRepo.search(status, from, to, nationalIdHash, PageRequest.of(page, size));
    }

    // ===== STATE TRANSITIONS ==============================================

    @Transactional
    @Override
    public Mono<Application> decide(UUID id,
                                    DecisionRequest req,
                                    String officerId,
                                    String officerName,
                                    int expectedVersion,
                                    String correlationId) {
        DecisionType dtype = req.getDecision();
        Status target = Status.REJECTED;

        return appRepo.updateStatus(id, target, expectedVersion)
                .flatMap(updated -> {
                    if (!updated) return Mono.error(new VersionConflictException());
                    return appRepo.findById(id); // re-load with new version/status
                })
                .flatMap(app -> persistDecision(app, dtype, req.getComments(), officerId, officerName)
                        .thenReturn(app))
                .flatMap(app -> messagingService.publishApplicationEvent(app, EventType.APPLICATION_DECISION_MADE, correlationId)
                        .thenReturn(app));
    }

    private Mono<Decision> persistDecision(Application app,
                                           DecisionType dtype,
                                           String comments,
                                           String officerId,
                                           String officerName) {
        Decision dec = Decision.builder()
                .id(UUID.randomUUID())
                .applicationId(app.getId())
                .decisionType(dtype)
                .comments(comments)
                .officerUserId(officerId)
                .officerName(officerName)
                .build();
        return decisionRepo.insert(dec);
    }


}