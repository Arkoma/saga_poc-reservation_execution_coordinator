package com.saga_poc.reservation_execution_coordinator.message;

import com.saga_poc.reservation_execution_coordinator.model.Reservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ReservationStatusProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public ReservationStatusProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(String topic, Reservation reservation) {

    }
}
