package com.saga_poc.reservation_execution_coordinator.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.saga_poc.reservation_execution_coordinator.model.CarReservation;
import com.saga_poc.reservation_execution_coordinator.model.CarReservationResponse;
import com.saga_poc.reservation_execution_coordinator.model.Endpoints;
import com.saga_poc.reservation_execution_coordinator.model.StatusEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
public class CarReservationServiceClientIT {

    private final WireMockServer carWireMockServer = new WireMockServer(Integer.parseInt(Endpoints.CAR_API_PORT));

    @Qualifier("com.saga_poc.reservation_execution_coordinator.client.CarReservationServiceClient")
    @Autowired
    CarReservationServiceClient underTest;
    private static final Long RESERVATION_ID = 1L;
    private static final String CAR_MAKE = "Ford";
    private static final String CAR_MODEL = "Model-T";
    private static final String CAR_AGENCY = "Hertz";
    private static final String CAR_RENTAL_DATE = "09 Feb 2022";
    private static final String CAR_RETURN_DATE = "12 Feb 2022";
    private static final CarReservation carReservation;

    static {
        try {
            carReservation = CarReservation.builder()
                    .reservationId(RESERVATION_ID)
                    .carMake(CAR_MAKE)
                    .carModel(CAR_MODEL)
                    .agency(CAR_AGENCY)
                    .checkinDate(new SimpleDateFormat("dd MMM yyyy").parse(CAR_RENTAL_DATE))
                    .checkoutDate(new SimpleDateFormat("dd MMM yyyy").parse(CAR_RETURN_DATE))
                    .build();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    private static final String CAR_RESERVATION_SUCCESS = "{\n" +
            "    \"id\": 4,\n" +
            "    \"status\": \"RESERVED\",\n" +
            "    \"reservationId\": 1,\n" +
            "    \"carId\": 1,\n" +
            "    \"carMake\": \"Ford\",\n" +
            "    \"carModel\": \"Model-T\",\n" +
            "    \"agency\": \"Hertz\",\n" +
            "    \"checkinDate\": \"2022-02-09T05:00:00.000+00:00\",\n" +
            "    \"checkoutDate\": \"2022-02-12T05:00:00.000+00:00\"\n" +
            "}";

    @BeforeEach
    void before() {
        if (!carWireMockServer.isRunning()) {
            carWireMockServer.start();
        }
    }

    @AfterEach
    void after() {
        if (carWireMockServer.isRunning()) {
            carWireMockServer.stop();
        }
    }

    @Test
    void makeReservationReturnsExpectedResponse() {
        CarReservationResponse expectedResponse = new CarReservationResponse();
        expectedResponse.setId(4L);
        expectedResponse.setReservationId(RESERVATION_ID);
        expectedResponse.setStatus(StatusEnum.RESERVED);
        expectedResponse.setCarId(1L);
        expectedResponse.setCarMake(CAR_MAKE);
        expectedResponse.setCarModel(CAR_MODEL);
        expectedResponse.setAgency(CAR_AGENCY);
        expectedResponse.setCheckinDate("2022-02-09T05:00:00.000+00:00");
        expectedResponse.setCheckoutDate("2022-02-12T05:00:00.000+00:00");
        carWireMockServer.stubFor(post("/reservation")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
                        .withBody(CAR_RESERVATION_SUCCESS)));
        CarReservationResponse response = underTest.makeReservation(carReservation);
        assertEquals(expectedResponse, response);
    }

    @Test
    public void makeReservationFallbackReturnsCanceledResponse() {
        carWireMockServer.stubFor(post("/reservation")
                .willReturn(aResponse()
                .withStatus(500)));
        CarReservationResponse expectedResponse = new CarReservationResponse();
        expectedResponse.setStatus(StatusEnum.CANCELED);

        CarReservationResponse actualResponse = underTest.makeReservation(carReservation);

        assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
    }
}
