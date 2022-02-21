package com.saga_poc.reservation_execution_coordinator.service;

import com.saga_poc.reservation_execution_coordinator.repository.ReservationStatusRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
class ReservationStepCoordinatorIT {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ReservationStepCoordinator underTest;

    @Autowired
    private ReservationStatusRepository repository;

    @Test
    void reservationStepCoordinatorIsABean() {
        assertTrue(applicationContext.containsBean("reservationStepCoordinator"));
    }

    @Test
    void setReservationStatusRepositoryInReservationStepCoordinator() {
        ReservationStatusRepository injectedRepository = (ReservationStatusRepository) ReflectionTestUtils
                .getField(underTest, "reservationStatusRepository");
        assertSame(repository, injectedRepository);
    }

}