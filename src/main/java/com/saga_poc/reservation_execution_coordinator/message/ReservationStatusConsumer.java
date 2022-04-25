package com.saga_poc.reservation_execution_coordinator.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saga_poc.reservation_execution_coordinator.model.Reservation;
import com.saga_poc.reservation_execution_coordinator.service.ReservationStepCoordinator;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;

@Component
public class ReservationStatusConsumer {

    private String payload = null;

    private final ReservationStepCoordinator reservationStepCoordinator;

    public ReservationStatusConsumer(ReservationStepCoordinator reservationStepCoordinator) {
        this.reservationStepCoordinator = reservationStepCoordinator;
    }

    @KafkaListener(topics = "${spring.kafka.template.default-topic}")
    public void receive(String payload) throws IOException, URISyntaxException, InterruptedException {
        setPayload(payload);
        Reservation reservation = new ObjectMapper().readValue(payload, Reservation.class);
        this.reservationStepCoordinator.handleReservation(reservation);
    }

    public String getPayload() {
        return this.payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
