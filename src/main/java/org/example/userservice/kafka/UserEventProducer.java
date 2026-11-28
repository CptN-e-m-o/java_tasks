
package org.example.userservice.kafka;

import org.example.userservice.event.UserEvent;
import org.example.userservice.event.UserOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserEventProducer {

    private static final Logger log =
            LoggerFactory.getLogger(UserEventProducer.class);

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;
    private final String topic;

    public UserEventProducer(
            KafkaTemplate<String, UserEvent> kafkaTemplate,
            @Value("${app.kafka.user-events-topic}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void userCreated(String email) {
        sendEvent(new UserEvent(UserOperation.CREATED, email));
    }

    public void userDeleted(String email) {
        sendEvent(new UserEvent(UserOperation.DELETED, email));
    }

    private void sendEvent(UserEvent event) {
        kafkaTemplate.send(topic, event.email(), event)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.error(
                                "Не удалось отправить событие {} для {}",
                                event.operation(),
                                event.email(),
                                exception
                        );
                    } else {
                        log.info(
                                "Событие {} для {} отправлено в Kafka",
                                event.operation(),
                                event.email()
                        );
                    }
                });
    }
}
