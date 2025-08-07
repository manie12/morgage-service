package io.bank.mortgage.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.bank.mortgage.datatype.EventType;
import io.bank.mortgage.domain.model.Application;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class MessagingService {

    private static final String TOPIC_NAME = "loan.applications";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;


    public Mono<Void> publishApplicationEvent(Application application, EventType type, String correlationId) {
        return Mono.defer(() -> {
            try {
                return sendToKafka(application, type, correlationId)
                        .onErrorResume(ex -> {
                            log.error("Kafka publish failed – using ActiveMQ fallback", ex);
                            return sendToActiveMq(application, type, correlationId);
                        });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private Mono<Void> sendToKafka(Application app, EventType type, String corr) throws Exception {
        String json = buildEnvelope(app, type, corr);
        ProducerRecord<String, String> rec =
                new ProducerRecord<>(TOPIC_NAME, app.getId().toString(), json);
        rec.headers().add(KafkaHeaders.CORRELATION_ID, asBytes(corr));

        var future = kafkaTemplate.send(rec); // Now returns CompletableFuture<SendResult<...>>

        return Mono.fromFuture(() -> future.thenAccept(result -> {
                    if (result != null) {
                        log.debug("Kafka ✔ topic={} partition={} offset={}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                }))
                .onErrorResume(ex -> {
                    log.error("Error sending to Kafka", ex);
                    return Mono.error(ex);
                })
                .then();
    }

    private Mono<Void> sendToActiveMq(Application app, EventType type, String corr) {
        return Mono.fromRunnable(() -> {
            try {
                jmsTemplate.convertAndSend(TOPIC_NAME, buildEnvelope(app, type, corr));
                log.debug("ActiveMQ ✔ destination={}", TOPIC_NAME);
            } catch (Exception jmsEx) {
                log.error("‼  ActiveMQ fallback failed – event lost", jmsEx);
            }
        });
    }

    private String buildEnvelope(Application app, EventType type, String correlationId) throws Exception {
        Map<String, Object> envelope = new HashMap<>();
        envelope.put("eventId", UUID.randomUUID());
        envelope.put("eventType", type.name());
        envelope.put("eventVersion", 1);
        envelope.put("occurredAt", Instant.now());
        envelope.put("correlationId", correlationId);
        envelope.put("aggregate", Map.of(
                "type", "Application",
                "id", app.getId(),
                "version", app.getVersion()
        ));
        envelope.put("payload", app);
        return objectMapper.writeValueAsString(envelope);
    }

    private byte[] asBytes(String str) {
        return str == null ? new byte[0] : str.getBytes();
    }
}