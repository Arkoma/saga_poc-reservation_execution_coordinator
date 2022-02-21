package com.saga_poc.reservation_execution_coordinator.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saga_poc.reservation_execution_coordinator.model.Reservation;
import com.saga_poc.reservation_execution_coordinator.model.StatusEnum;
import com.saga_poc.reservation_execution_coordinator.service.ReservationStepCoordinator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
class ReservationStatusMessagingIT {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ReservationStepCoordinator reservationStepCoordinator;

    @Autowired
    private ReservationStatusProducer producerUnderTest;

    @Autowired
    private ReservationStatusConsumer consumerUnderTest;

    @Value("${spring.kafka.template.default-topic}")
    private String topic;

    @Test
    void reservationStatusProducerIsABean() {
        assertTrue(applicationContext.containsBean("reservationStatusProducer"));
    }

    @Test
    void reservationStatusConsumerIsABean() {
        assertTrue(applicationContext.containsBean("reservationStatusConsumer"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void setKafkaTemplateInReservationStatusProducer() {
        KafkaTemplate<String, String> injectedKafkaTemplate = (KafkaTemplate<String, String>) ReflectionTestUtils
                                                                            .getField(producerUnderTest, "kafkaTemplate");
        assertSame(kafkaTemplate, injectedKafkaTemplate);
    }


    @Test
    void setReservationStepCoordinatorInReservationStatusConsumer() {
        ReservationStepCoordinator injectedReservationStepCoordinator = (ReservationStepCoordinator) ReflectionTestUtils
                .getField(consumerUnderTest, "reservationStepCoordinator");
        assertSame(reservationStepCoordinator, injectedReservationStepCoordinator);
    }

    @Test
    void sentReservationStatusIsReceived() throws InterruptedException, JsonProcessingException {
        final String hotelName = "Holiday Inn";
        final String carMake = "Ford";
        final String carModel = "Model-T";
        final String customerName = "Tom Brady";
        final long id = 1L;
        Reservation reservation = Reservation.builder()
                .id(id)
                .customerName(customerName)
                .hotelName(hotelName)
                .carMake(carMake)
                .carModel(carModel)
                .status(StatusEnum.PENDING)
                .build();
        producerUnderTest.send(topic, reservation);
        String json = null;
        while (json == null) {
            Thread.sleep(500);
            json = consumerUnderTest.getPayload();
        }
        Reservation consumedReservation = new ObjectMapper().readValue(json, Reservation.class);
        assertAll(() -> {
            assertEquals(id, consumedReservation.getId());
            assertEquals(customerName, consumedReservation.getCustomerName());
            assertEquals(hotelName, consumedReservation.getHotelName());
            assertEquals(carMake, consumedReservation.getCarMake());
            assertEquals(carModel, consumedReservation.getCarModel());
            assertEquals(StatusEnum.PENDING, consumedReservation.getStatus());
        });
    }
}