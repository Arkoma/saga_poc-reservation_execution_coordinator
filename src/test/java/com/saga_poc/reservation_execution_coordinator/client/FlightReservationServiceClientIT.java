package com.saga_poc.reservation_execution_coordinator.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Fault;
import com.saga_poc.reservation_execution_coordinator.model.Endpoints;
import com.saga_poc.reservation_execution_coordinator.model.FlightReservation;
import com.saga_poc.reservation_execution_coordinator.model.FlightReservationResponse;
import com.saga_poc.reservation_execution_coordinator.model.StatusEnum;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
class FlightReservationServiceClientIT {

    private WireMockServer flightWireMockServer = new WireMockServer(Integer.parseInt(Endpoints.FLIGHT_API_PORT));

    @Qualifier("com.saga_poc.reservation_execution_coordinator.client.FlightReservationServiceClient")
    @Autowired
    FlightReservationServiceClient underTest;

    private static final Long RESERVATION_ID = 1L;
    private static final int FLIGHT_NUMBER = 880;
    private static final String SEAT_NUMBER = "15B";
    private static final String FLIGHT_DEPARTURE_DATE = "12 Apr 2022";
    private static final String FLIGHT_RETURN_DATE = "19 Apr 2022";
    private static final FlightReservation flightReservation;

    static {
        try {
            flightReservation = FlightReservation.builder()
                    .reservationId(RESERVATION_ID)
                    .flightNumber(FLIGHT_NUMBER)
                    .seatNumber(SEAT_NUMBER)
                    .departureDate(new SimpleDateFormat("dd MMM yyyy").parse(FLIGHT_DEPARTURE_DATE).getTime())
                    .returnDate(new SimpleDateFormat("dd MMM yyyy").parse(FLIGHT_RETURN_DATE).getTime())
                    .build();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String FLIGHT_RESERVATION_SUCCESS = "{\n" +
            "    \"id\": 5,\n" +
            "    \"status\": \"RESERVED\",\n" +
            "    \"reservationId\": 1,\n" +
            "    \"flightId\": 1,\n" +
            "    \"flightNumber\": \"880\",\n" +
            "    \"seatNumber\": \"15B\",\n" +
            "    \"departureDate\": \"2022-02-09T05:00:00.000+00:00\",\n" +
            "    \"returnDate\": \"2022-02-12T05:00:00.000+00:00\"\n" +
            "}";

    @Test
    void makeReservationHappy() throws ParseException {
        FlightReservationResponse expectedResponse = new FlightReservationResponse();
        expectedResponse.setId(5L);
        expectedResponse.setStatus(StatusEnum.RESERVED);
        expectedResponse.setFlightId(1L);
        expectedResponse.setFlightNumber(String.valueOf(FLIGHT_NUMBER));
        expectedResponse.setReservationId(RESERVATION_ID);
        expectedResponse.setSeatNumber(SEAT_NUMBER);
        expectedResponse.setDepartureDate("2022-02-09T05:00:00.000+00:00");
        expectedResponse.setReturnDate("2022-02-12T05:00:00.000+00:00");
        flightWireMockServer.stubFor(post("/reservation")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
                        .withBody(FLIGHT_RESERVATION_SUCCESS)));

        flightWireMockServer.start();
        FlightReservationResponse response = underTest.makeReservation(flightReservation);
        assertEquals(expectedResponse, response);
        flightWireMockServer.stop();
    }

    @Test
    public void makeReservationSad() throws Exception {
        flightWireMockServer.stubFor(post("/reservation")
                .willReturn(aResponse()
                        .withFault(Fault.RANDOM_DATA_THEN_CLOSE)));

        flightWireMockServer.start();

        FlightReservationResponse expectedResponse = new FlightReservationResponse();
        expectedResponse.setStatus(StatusEnum.CANCELED);
        AtomicReference<FlightReservationResponse> actualResponse = new AtomicReference<>();
        try {
            actualResponse.set(underTest.makeReservation(flightReservation));
            fail("Test scenario expects Feign Client Exception at this point");
        } catch (FeignException e) {
            assertEquals(expectedResponse.getStatus(), actualResponse.get().getStatus());
        }

        flightWireMockServer.stop();
    }
}
