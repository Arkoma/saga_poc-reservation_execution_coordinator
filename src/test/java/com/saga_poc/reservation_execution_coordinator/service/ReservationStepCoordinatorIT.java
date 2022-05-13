package com.saga_poc.reservation_execution_coordinator.service;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.saga_poc.reservation_execution_coordinator.model.CoordinationStep;
import com.saga_poc.reservation_execution_coordinator.model.Endpoints;
import com.saga_poc.reservation_execution_coordinator.model.Reservation;
import com.saga_poc.reservation_execution_coordinator.model.ReservationStatus;
import com.saga_poc.reservation_execution_coordinator.model.StatusEnum;
import com.saga_poc.reservation_execution_coordinator.repository.ReservationStatusRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
class ReservationStepCoordinatorIT {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ReservationStepCoordinator underTest;

    @Autowired
    private ReservationStatusRepository reservationStatusRepository;

    @RegisterExtension
    static WireMockExtension hotelWireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().port(Integer.parseInt(Endpoints.HOTEL_API_PORT)))
            .build();

    @RegisterExtension
    static WireMockExtension carWireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().port(Integer.parseInt(Endpoints.CAR_API_PORT)))
            .build();

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
    private static final String HOTEL_RESERVATION_201_RESPONSE = "{\n" +
            "    \"id\": 4,\n" +
            "    \"reservationId\": 1,\n" +
            "    \"status\": \"RESERVED\",\n" +
            "    \"hotelId\": 1,\n" +
            "    \"hotelName\": \"Holiday Inn\",\n" +
            "    \"room\": 666,\n" +
            "    \"checkinDate\": \"2022-04-12T04:00:00.000+00:00\",\n" +
            "    \"checkoutDate\": \"2022-04-19T04:00:00.000+00:00\"\n" +
            "}";
    private static final String CAR_RESERVATION_201_RESPONSE = "{\n" +
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
        hotelWireMock.stubFor(post("/reservation")
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
                        .withBody(HOTEL_RESERVATION_201_RESPONSE)));
        carWireMock.stubFor(post("/reservation")
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
                        .withBody(CAR_RESERVATION_201_RESPONSE)));
    }

    @AfterEach
    void tearDown() {
        this.reservationStatusRepository.deleteAll();
    }

    @Test
    void reservationStepCoordinatorIsABean() {
        assertTrue(applicationContext.containsBean("reservationStepCoordinator"));
    }

    @Test
    void setReservationStatusRepositoryInReservationStepCoordinator() {
        ReservationStatusRepository injectedRepository = (ReservationStatusRepository) ReflectionTestUtils
                .getField(underTest, "reservationStatusRepository");
        assertSame(reservationStatusRepository, injectedRepository);
    }

    @Test
    void handleReservationWithHotel201ResponseSetsCoordinationStepToCarReservation() {
        assertFalse(reservationStatusRepository.findByReservationId(1L).isPresent());

        underTest.handleReservation(reservation);

        final Optional<ReservationStatus> byReservationId = reservationStatusRepository.findByReservationId(1L);
        ReservationStatus reservationStatus = null;
        if (byReservationId.isPresent()) {
            reservationStatus = byReservationId.get();
        }
        assert reservationStatus != null;
        assertEquals(CoordinationStep.RESERVE_CAR, reservationStatus.getCoordinationStep());
    }

    @Test
    void handleReservationWithCar201ResponseSetsCoordinationStepToFlightReservation() {
        // assemble
        assertFalse(reservationStatusRepository.findByReservationId(1L).isPresent());
        ReservationStatus status = new ReservationStatus();
        status.setReservationId(reservation.getId());
        status.setCoordinationStep(CoordinationStep.RESERVE_CAR);
        reservationStatusRepository.save(status);

        //act
        underTest.handleReservation(reservation);

        final Optional<ReservationStatus> byReservationId = reservationStatusRepository.findByReservationId(1L);
        ReservationStatus reservationStatus = null;
        if (byReservationId.isPresent()) {
            reservationStatus = byReservationId.get();
        }
        // assert
        assert reservationStatus != null;
        assertEquals(CoordinationStep.RESERVE_FLIGHT, reservationStatus.getCoordinationStep());
    }
}