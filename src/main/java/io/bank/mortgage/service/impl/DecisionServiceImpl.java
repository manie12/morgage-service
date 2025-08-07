package io.bank.mortgage.service.impl;

import io.bank.mortgage.datatype.DecisionType;
import io.bank.mortgage.datatype.Status;
import io.bank.mortgage.domain.model.Application;
import io.bank.mortgage.domain.model.Decision;
import io.bank.mortgage.domain.repo.ApplicationRepository;
import io.bank.mortgage.domain.repo.DecisionRepository;
import io.bank.mortgage.dto.DecisionRequest;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

import static org.springframework.http.ResponseEntity.notFound;

@Service
@RequiredArgsConstructor
public class DecisionServiceImpl {
  private final ApplicationRepository repo;
  private final DecisionRepository decisions;
  private final OutboxServiceImpl outbox;

  @Transactional
  public Application decide(UUID id, DecisionRequest req, String officerUserId, String officerName, int expectedVersion, String correlationId) {
    var app = repo.findById(id).orElseThrow(() -> notFound(id));
    if (!Objects.equals(app.getVersion(), expectedVersion)) throw new VersionConflictException();
    requireState(app.getStatus() == Status.UNDER_REVIEW, "Only UNDER_REVIEW can be decided");
    if (req.decision()== DecisionType.REJECTED && (req.comments()==null || req.comments().isBlank()))
      throw new ValidationException("Rejection requires comments");

    var decision = new Decision(/* set fields */);
    decisions.save(decision);

    app.setStatus(req.decision()==DecisionType.APPROVED ? Status.APPROVED : Status.REJECTED);
    app.setDecidedAt(Instant.now());
    var saved = repo.save(app);

    outbox.applicationEvent(id, EventType.APPLICATION_DECIDED, correlationId);
    return saved;
  }
}