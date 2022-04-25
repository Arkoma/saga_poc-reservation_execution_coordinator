package com.saga_poc.reservation_execution_coordinator.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saga_poc.reservation_execution_coordinator.model.Reservation;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ReservationStatusProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public ReservationStatusProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(String topic, Reservation reservation) throws JsonProcessingException {
        String payload = new ObjectMapper().writeValueAsString(reservation);
        this.kafkaTemplate.send(topic, payload);
    }
}
