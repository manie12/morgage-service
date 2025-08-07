package io.bank.mortgage.repo;

import io.bank.mortgage.domain.model.Application;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ApplicationRepository extends
        org.springframework.data.repository.reactive.ReactiveCrudRepository<Application, UUID>,
        ApplicationRepositoryCustom {

  /** Idempotency helper – lookup by same user + external reference. */
  Mono<Application> findFirstByApplicantUserIdAndExternalRef(String applicantUserId, String externalRef);
}
