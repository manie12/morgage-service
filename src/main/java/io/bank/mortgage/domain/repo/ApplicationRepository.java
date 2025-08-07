package io.bank.mortgage.domain.repo;

import io.bank.mortgage.domain.model.Application;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ApplicationRepository extends ReactiveCrudRepository<Application, UUID>, ProductRepositoryCustom {
  Mono<Application> findByUserIdAndSku(UUID userId, String sku);

}