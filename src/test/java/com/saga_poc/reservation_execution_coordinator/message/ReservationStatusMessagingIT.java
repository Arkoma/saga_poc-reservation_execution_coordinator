package com.saga_poc.reservation_execution_coordinator.message;

import com.saga_poc.reservation_execution_coordinator.model.Reservation;
import com.saga_poc.reservation_execution_coordinator.model.StatusEnum;
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
    private ReservationStatusProducer producerUnderTest;

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
    void sentReservationStatusIsReceived() throws InterruptedException {
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
        Thread.sleep(5000);

    }
}