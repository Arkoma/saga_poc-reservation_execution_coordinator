package com.saga_poc.reservation_execution_coordinator.service;

import com.saga_poc.reservation_execution_coordinator.model.CoordinationStep;
import com.saga_poc.reservation_execution_coordinator.model.Reservation;
import com.saga_poc.reservation_execution_coordinator.model.ReservationStatus;
import com.saga_poc.reservation_execution_coordinator.model.StatusEnum;
import com.saga_poc.reservation_execution_coordinator.repository.ReservationStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationStepCoordinatorTest {

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


    @InjectMocks
    private ReservationStepCoordinator underTest;

    @Mock
    private ReservationStatusRepository mockRepository;

    @Captor
    private ArgumentCaptor<ReservationStatus> reservationStatusArgumentCaptor;

    private Reservation reservation;
    private ReservationStatus reservationStatus;

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
        reservationStatus = new ReservationStatus();
        reservationStatus.setId(1L);
        reservationStatus.setReservationId(1L);
        reservationStatus.setCoordinationStep(CoordinationStep.RESERVE_HOTEL);
    }
    @Test
    void takeNextStepChecksToSeeIfReservationHasReservationStatus() throws URISyntaxException, IOException, InterruptedException {
        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> mockHttpResponse = mock(HttpResponse.class);
        try(MockedStatic<HttpClient> httpClient = Mockito.mockStatic(HttpClient.class)) {
            httpClient.when(HttpClient::newHttpClient).thenReturn(mockHttpClient);
            when(mockRepository.findByReservationId(anyLong())).thenReturn(Optional.empty(), Optional.of(reservationStatus));
            when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockHttpResponse);
            when(mockHttpResponse.body()).thenReturn(HOTEL_RESERVATION_201_RESPONSE);
            underTest.handleReservation(reservation);
            verify(mockRepository, atLeastOnce()).findByReservationId(anyLong());
        }
    }

    @Test
    void setsReservationStatusAsSaveHotelWhenNoReservationStatusFound() throws URISyntaxException, IOException, InterruptedException {
        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> mockHttpResponse = mock(HttpResponse.class);
        try(MockedStatic<HttpClient> httpClient = Mockito.mockStatic(HttpClient.class)) {
            httpClient.when(HttpClient::newHttpClient).thenReturn(mockHttpClient);
            when(mockRepository.findByReservationId(anyLong())).thenReturn(Optional.empty(), Optional.of(reservationStatus));
            when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockHttpResponse);
            when(mockHttpResponse.body()).thenReturn(HOTEL_RESERVATION_201_RESPONSE);
            underTest.handleReservation(reservation);
            assertAll(() -> {
                verify(mockRepository, atLeastOnce()).findByReservationId(anyLong());
                verify(mockRepository, atLeastOnce()).save(reservationStatusArgumentCaptor.capture());
                final ReservationStatus reservationStatus = reservationStatusArgumentCaptor.getAllValues().get(0);
                CoordinationStep nextStep = reservationStatus.getCoordinationStep();
                Long reservationIdOfNextStep = reservationStatus.getReservationId();
                assertEquals(CoordinationStep.RESERVE_HOTEL, nextStep);
                assertEquals(ID, reservationIdOfNextStep);
            });
        }
    }


}