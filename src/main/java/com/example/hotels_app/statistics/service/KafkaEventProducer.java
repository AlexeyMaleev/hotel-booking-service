package com.example.hotels_app.statistics.service;

import com.example.hotels_app.model.event.BookingEvent;
import com.example.hotels_app.model.event.UserRegistrationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.kafkaMessageTopic}")
    private String topic;

    public void sendRegistrationEvent(Long userId) {
        kafkaTemplate.send(topic, new UserRegistrationEvent(userId));
    }

    public void sendBookingEvent(BookingEvent event) {
        kafkaTemplate.send(topic, event);
    }
}
