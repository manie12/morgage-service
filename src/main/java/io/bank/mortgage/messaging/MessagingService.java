package io.bank.mortgage.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.bank.mortgage.domain.model.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.jms.core.JmsTemplate;

@Service
public class MessagingService {

    private static final Logger log = LoggerFactory.getLogger(MessagingService.class);
    private static final String TOPIC_NAME = "loan.applications";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void publishApplicationEvent(Application application, String eventType) {
        try {
            String payload = objectMapper.writeValueAsString(application);
            log.info("Attempting to publish event to Kafka...");
            kafkaTemplate.send(TOPIC_NAME, application.getId().toString(), payload);
        } catch (Exception e) {
            log.error("Kafka unavailable. Falling back to ActiveMQ. Error: {}", e.getMessage());
            try {
                jmsTemplate.convertAndSend(TOPIC_NAME, application);
            } catch (Exception jmsException) {
                log.error("Failed to publish event to both Kafka and ActiveMQ. Event may be lost.", jmsException);
            }
        }
    }
}
