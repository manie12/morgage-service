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


    public void publishApplicationEvent(Application application, EventType type, String correlationId) {
        try {
            String payload = buildEnvelope(application, type, correlationId);

            ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, application.getId().toString(), payload);
            record.headers().add(KafkaHeaders.CORRELATION_ID, asBytes(correlationId));
            kafkaTemplate.send(record);
            log.debug("Published {} to Kafka topic {}", type, TOPIC_NAME);

        } catch (Exception kafkaEx) {
            log.error("Kafka publish failed – falling back to ActiveMQ. {}", kafkaEx.getMessage());
            try {
                jmsTemplate.convertAndSend(TOPIC_NAME, buildEnvelope(application, type, correlationId));
                log.debug("Published {} to ActiveMQ topic {}", type, TOPIC_NAME);
            } catch (Exception jmsEx) {
                log.error("‼️ Failed to publish event to both Kafka and ActiveMQ – event lost", jmsEx);
            }
        }
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