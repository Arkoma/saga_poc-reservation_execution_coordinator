package com.saga_poc.reservation_execution_coordinator.message;

import com.saga_poc.reservation_execution_coordinator.service.ReservationStepCoordinator;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ReservationStatusConsumer {

    private String payload = null;

    private final ReservationStepCoordinator reservationStepCoordinator;

    public ReservationStatusConsumer(ReservationStepCoordinator reservationStepCoordinator) {
        this.reservationStepCoordinator = reservationStepCoordinator;
    }

    @KafkaListener(topics = "${spring.kafka.template.default-topic}")
    public void receive(String reservation) {
        setPayload(reservation);
    }

    public String getPayload() {
        return this.payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
