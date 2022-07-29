package com.saga_poc.reservation_execution_coordinator.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.saga_poc.reservation_execution_coordinator.model.Endpoints;
import com.saga_poc.reservation_execution_coordinator.model.HotelReservation;
import com.saga_poc.reservation_execution_coordinator.model.HotelReservationResponse;
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
class HotelReservationServiceClientIT {

    private final WireMockServer hotelWireMockServer = new WireMockServer(Integer.parseInt(Endpoints.HOTEL_API_PORT));

    @Qualifier("com.saga_poc.reservation_execution_coordinator.client.HotelReservationServiceClient")
    @Autowired
    HotelReservationServiceClient underTest;

    private static final Long RESERVATION_ID = 1L;
    private static final String HOTEL_NAME = "Holiday Inn";
    private static final int HOTEL_ROOM = 666;
    private static final String HOTEL_CHECKIN_DATE = "09 Feb 2022";
    private static final String HOTEL_CHECKOUT_DATE = "12 Feb 2022";
    private static final HotelReservation hotelReservation;

    static {
        try {
            hotelReservation = HotelReservation.builder()
                    .reservationId(RESERVATION_ID)
                    .hotelName(HOTEL_NAME)
                    .room(HOTEL_ROOM)
                    .checkinDate(new SimpleDateFormat("dd MMM yyyy").parse(HOTEL_CHECKIN_DATE).getTime())
                    .checkoutDate(new SimpleDateFormat("dd MMM yyyy").parse(HOTEL_CHECKOUT_DATE).getTime())
                    .build();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    private static final String HOTEL_RESERVATION_SUCCESS = "{\n" +
            "    \"id\": 4,\n" +
            "    \"reservationId\": 1,\n" +
            "    \"status\": \"RESERVED\",\n" +
            "    \"hotelId\": 1,\n" +
            "    \"hotelName\": \"Holiday Inn\",\n" +
            "    \"room\": 666,\n" +
            "    \"checkinDate\": \"2022-02-09T05:00:00.000+00:00\",\n" +
            "    \"checkoutDate\": \"2022-02-12T05:00:00.000+00:00\"\n" +
            "}";
    @BeforeEach
    void setUp() {
        if (!hotelWireMockServer.isRunning()) {
            hotelWireMockServer.start();
        }
    }

    @AfterEach
    void tearDown() {
        if(hotelWireMockServer.isRunning()) {
            hotelWireMockServer.stop();
        }
    }

    @Test
    void makeReservationReturnsExpectedResponse() {
        HotelReservationResponse expectedResponse = new HotelReservationResponse();
        expectedResponse.setReservationId(RESERVATION_ID);
        expectedResponse.setId(4L);
        expectedResponse.setStatus(StatusEnum.RESERVED);
        expectedResponse.setHotelId(1L);
        expectedResponse.setHotelName(HOTEL_NAME);
        expectedResponse.setRoom(HOTEL_ROOM);
        expectedResponse.setCheckinDate("2022-02-09T05:00:00.000+00:00");
        expectedResponse.setCheckoutDate("2022-02-12T05:00:00.000+00:00");
        hotelWireMockServer.stubFor(post("/reservation")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
                        .withBody(HOTEL_RESERVATION_SUCCESS)));

        HotelReservationResponse response = underTest.makeReservation(hotelReservation);

        assertEquals(expectedResponse, response);

    }

    @Test
    public void makeReservationFallbackReturnsCanceledResponse() {
        hotelWireMockServer.stubFor(post("/reservation")
                .willReturn(aResponse()
                        .withStatus(500)));
        HotelReservationResponse expectedResponse = new HotelReservationResponse();
        expectedResponse.setStatus(StatusEnum.CANCELED);

        HotelReservationResponse actualResponse = underTest.makeReservation(hotelReservation);

        assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
    }
}