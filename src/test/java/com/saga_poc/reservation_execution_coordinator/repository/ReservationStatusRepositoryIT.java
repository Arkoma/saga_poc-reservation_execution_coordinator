package com.saga_poc.reservation_execution_coordinator.repository;

import com.saga_poc.reservation_execution_coordinator.model.CoordinationStep;
import com.saga_poc.reservation_execution_coordinator.model.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
class ReservationStatusRepositoryIT {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ReservationStatusRepository underTest;

    @BeforeEach
    void setup() {
        underTest.deleteAll();
    }

    @Test
    public void reservationStatusRepositoryIsABean() {
        assertTrue(applicationContext.containsBean("reservationStatusRepository"));
    }

    @Test
    public void canRetrieveStatusByReservationId() {
        final long reservationId = 1L;
        ReservationStatus reservationStatus = new ReservationStatus();
        reservationStatus.setReservationId(reservationId);
        reservationStatus.setCoordinationStep(CoordinationStep.CANCEL_HOTEL);
        ReservationStatus saved = underTest.save(reservationStatus);
        ReservationStatus found = underTest.findByReservationId(reservationId).orElse(null);
        assert found != null;
        assertEquals(saved.getId(), found.getId());
        assertEquals(reservationId, found.getReservationId());
    }
}