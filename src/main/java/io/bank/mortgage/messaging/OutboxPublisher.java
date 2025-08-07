//package io.bank.mortgage.messaging;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//@Service
//@RequiredArgsConstructor
//public class OutboxPublisher {
//  private final KafkaTemplate<String,String> kafka;
//  private final OutboxEventRepository repo;
//
//  @Scheduled(fixedDelayString = "${outbox.publisher.interval:1000}")
//  @Transactional
//  public void publishBatch() {
//    var batch = repo.findTop100ByPublishedAtIsNullOrderByOccurredAtAsc();
//    for (var evt : batch) {
//      kafka.send("loan.applications", evt.getAggregateId().toString(), evt.getPayload().toString())
//           .completable()
//           .whenComplete((md,ex) -> {
//             if (ex==null) { evt.setPublishedAt(Instant.now()); repo.save(evt); }
//             else { evt.setAttempts(evt.getAttempts()+1); repo.save(evt); }
//           });
//    }
//  }
//}