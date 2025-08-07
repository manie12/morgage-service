package io.bank.mortgage.service.impl;

import io.bank.mortgage.domain.model.Application;
import io.bank.mortgage.domain.repo.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImp {
    private final ApplicationRepository repo;
    private final OutboxService outbox;
    private final NationalIdServiceImpl nid;
    private final DocumentService docs;

    @Transactional
    public Application create(New req, String applicantUserId, String correlationId) {
        var app = new Application();
        app.setApplicantUserId(applicantUserId);
        app.setNationalIdHash(nid.hash(req.nationalId()));
        app.setNationalIdEnc(nid.encrypt(req.nationalId()));
        app.setExternalRef(req.externalRef());
        app.setLoanAmount(req.loanAmount());
        app.setCurrency(req.currency());
        app.setIncome(defaultZero(req.income()));
        app.setLiabilities(defaultZero(req.liabilities()));
        app.setPropertyAddress(req.propertyAddress());
        app.setPropertyValue(defaultZero(req.propertyValue()));
        app.setPropertyType(req.propertyType());
        app.setStatus(Status.SUBMITTED); // or DRAFT per config
        app.setSubmittedAt(Instant.now());
        var saved = repo.save(app);

        if (req.documents() != null && !req.documents().isEmpty()) {
            docs.attach(saved.getId(), req.documents());
        }

        outbox.applicationEvent(saved.getId(), EventType.APPLICATION_CREATED, correlationId);
        return saved;
    }

    @Transactional(readOnly = true)
    public Application getOwned(UUID id, String requesterId, boolean officer) {
        var app = repo.findById(id).orElseThrow(() -> notFound(id));
        if (!officer && !app.getApplicantUserId().equals(requesterId)) throw forbidden();
        return app;
    }

    @Transactional
    public Application markUnderReview(UUID id, String officerId) {
        var app = repo.findById(id).orElseThrow(() -> notFound(id));
        requireState(app.getStatus() == Status.SUBMITTED, "Only SUBMITTED → UNDER_REVIEW");
        app.setStatus(Status.UNDER_REVIEW);
        var saved = repo.save(app);
        outbox.applicationEvent(id, EventType.APPLICATION_UPDATED, null);
        return saved;
    }
}