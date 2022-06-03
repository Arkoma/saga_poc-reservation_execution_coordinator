package com.saga_poc.reservation_execution_coordinator.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.saga_poc.reservation_execution_coordinator.model.Endpoints;
import com.saga_poc.reservation_execution_coordinator.model.Reservation;
import com.saga_poc.reservation_execution_coordinator.model.StatusEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
class ReservationStepCoordinatorIT {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ReservationStepCoordinator underTest;

//    @RegisterExtension
//    static WireMockExtension hotelWireMock = WireMockExtension.newInstance()
//            .options(wireMockConfig().port(Integer.parseInt(Endpoints.HOTEL_API_PORT)))
//            .build();
//
//    @RegisterExtension
//    static WireMockExtension carWireMock = WireMockExtension.newInstance()
//            .options(wireMockConfig().port(Integer.parseInt(Endpoints.CAR_API_PORT)))
//            .build();
//
//    @RegisterExtension
//    static WireMockExtension flightWireMock = WireMockExtension.newInstance()
//            .options(wireMockConfig().port(Integer.parseInt(Endpoints.FLIGHT_API_PORT)))
//            .build();

    private WireMockServer hotelWireMockServer = new WireMockServer(Integer.parseInt(Endpoints.HOTEL_API_PORT));
    private WireMockServer carWireMockServer = new WireMockServer(Integer.parseInt(Endpoints.CAR_API_PORT));
    private WireMockServer flightWireMockServer = new WireMockServer(Integer.parseInt(Endpoints.FLIGHT_API_PORT));


    private Reservation reservation;
    private static final long ID = 1L;
    private static final String CUSTOMER_NAME = "Tom Brady";
    private static final String HOTEL_NAME = "Holiday Inn";
    private static final int HOTEL_ROOM = 666;
    private static final String HOTEL_CHECKIN_DATE = "12 Apr 2022";
    private static final String HOTEL_CHECKOUT_DATE = "19 Apr 2022";
    private static final String CAR_MAKE = "Ford";
    private static final String CAR_MODEL = "Model-T";
    private static final String CAR_AGENCY = "Hertz";
    private static final String CAR_RENTAL_DATE = "12 Apr 2022";
    private static final String CAR_RETURN_DATE = "19 Apr 2022";
    private static final String FLIGHT_NUMBER = "880";
    private static final String SEAT_NUMBER = "14B";
    private static final String FLIGHT_DEPARTURE_DATE = "12 Apr 2022";
    private static final String FLIGHT_RETURN_DATE = "19 Apr 2022";
    private static final String HOTEL_RESERVATION_SUCCESS = "{\n" +
            "    \"id\": 4,\n" +
            "    \"reservationId\": 1,\n" +
            "    \"status\": \"RESERVED\",\n" +
            "    \"hotelId\": 1,\n" +
            "    \"hotelName\": \"Holiday Inn\",\n" +
            "    \"room\": 666,\n" +
            "    \"checkinDate\": \"2022-04-12T04:00:00.000+00:00\",\n" +
            "    \"checkoutDate\": \"2022-04-19T04:00:00.000+00:00\"\n" +
            "}";
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
    private static final String FLIGHT_RESERVATION_SUCCESS = "{\n" +
            "    \"id\": 5,\n" +
            "    \"status\": \"RESERVED\",\n" +
            "    \"reservationId\": 1,\n" +
            "    \"flightId\": 1,\n" +
            "    \"flightNumber\": \"880\",\n" +
            "    \"seatNumber\": \"15b\",\n" +
            "    \"departureDate\": \"2022-02-09T05:00:00.000+00:00\",\n" +
            "    \"returnDate\": \"2022-02-12T05:00:00.000+00:00\"\n" +
            "}";
    private static final String RESERVATION_ERROR = "{\n" +
            "    \"timestamp\": \"2022-05-21T01:23:23.417+00:00\",\n" +
            "    \"status\": 415,\n" +
            "    \"error\": \"Unsupported Media Type\",\n" +
            "    \"path\": \"/reservation\"\n" +
            "}";

    @BeforeEach
    void setup() throws ParseException {
        reservation = Reservation.builder()
                .id(ID)
                .customerName(CUSTOMER_NAME)
                .hotelName(HOTEL_NAME)
                .room(HOTEL_ROOM)
                .hotelCheckinDate(new SimpleDateFormat("dd MMM yyyy").parse(HOTEL_CHECKIN_DATE))
                .hotelCheckoutDate(new SimpleDateFormat("dd MMM yyyy").parse(HOTEL_CHECKOUT_DATE))
                .hotelReservationId(null)
                .carMake(CAR_MAKE)
                .carModel(CAR_MODEL)
                .carAgency(CAR_AGENCY)
                .carRentalDate(new SimpleDateFormat("dd MMM yyyy").parse(CAR_RENTAL_DATE))
                .carReturnDate(new SimpleDateFormat("dd MMM yyyy").parse(CAR_RETURN_DATE))
                .carReservationId(null)
                .flightNumber(FLIGHT_NUMBER)
                .seatNumber(SEAT_NUMBER)
                .flightDepartureDate(new SimpleDateFormat("dd MMM yyyy").parse(FLIGHT_DEPARTURE_DATE))
                .flightReturnDate(new SimpleDateFormat("dd MMM yyyy").parse(FLIGHT_RETURN_DATE))
                .flightReservationId(null)
                .status(StatusEnum.PENDING)
                .build();
        hotelWireMockServer.stubFor(post("/reservation")
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
                        .withBody(HOTEL_RESERVATION_SUCCESS)));
        carWireMockServer.stubFor(post("/reservation")
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
                        .withBody(CAR_RESERVATION_SUCCESS)));
        flightWireMockServer.stubFor(post("/reservation")
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
                        .withBody(FLIGHT_RESERVATION_SUCCESS)));
        flightWireMockServer.stubFor(delete("/reservation/5").willReturn(aResponse().withStatus(204)));
        carWireMockServer.stubFor(delete("/reservation/4").willReturn(aResponse().withStatus(204)));
        hotelWireMockServer.stubFor(delete("/reservation/4").willReturn(aResponse().withStatus(204)));
        hotelWireMockServer.start();
        carWireMockServer.start();
        flightWireMockServer.start();
    }

    @AfterEach
    void tearDown() {
        hotelWireMockServer.stop();
        carWireMockServer.stop();
        flightWireMockServer.stop();
    }

    @Test
    void reservationStepCoordinatorIsABean() {
        assertTrue(applicationContext.containsBean("reservationStepCoordinator"));
    }

    @Test
    void handleReservationWith201ResponsesCallsEachService() {
        underTest.handleReservation(reservation);
        hotelWireMockServer.verify(exactly(1), postRequestedFor(urlEqualTo("/reservation")));
        carWireMockServer.verify(exactly(1), postRequestedFor(urlEqualTo("/reservation")));
        flightWireMockServer.verify(exactly(1), postRequestedFor(urlEqualTo("/reservation")));
    }

    @Test
    void handleReservationWithHotelAndCarSuccessButFlightErrorWalksBackReservation() {
        flightWireMockServer.stubFor(post("/reservation")
                .willReturn(aResponse()
                        .withStatus(500)));

        underTest.handleReservation(reservation);

        hotelWireMockServer.verify(exactly(1), postRequestedFor(urlEqualTo("/reservation")));
        carWireMockServer.verify(exactly(1), postRequestedFor(urlEqualTo("/reservation")));
        flightWireMockServer.verify(exactly(1), postRequestedFor(urlEqualTo("/reservation")));
        flightWireMockServer.verify(exactly(1), deleteRequestedFor(urlEqualTo("/reservation/5")));
        carWireMockServer.verify(exactly(1), deleteRequestedFor(urlEqualTo("/reservation/4")));
        hotelWireMockServer.verify(exactly(1), deleteRequestedFor(urlEqualTo("/reservation/4")));
    }
}