package com.saga_poc.reservation_execution_coordinator.service;

import com.saga_poc.reservation_execution_coordinator.client.CarReservationServiceClient;
import com.saga_poc.reservation_execution_coordinator.client.HotelReservationServiceClient;
import com.saga_poc.reservation_execution_coordinator.model.CarReservation;
import com.saga_poc.reservation_execution_coordinator.model.CarReservationResponse;
import com.saga_poc.reservation_execution_coordinator.model.CoordinationStep;
import com.saga_poc.reservation_execution_coordinator.model.HotelReservation;
import com.saga_poc.reservation_execution_coordinator.model.HotelReservationResponse;
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
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
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

    @InjectMocks
    private ReservationStepCoordinator underTest;

    @Mock
    private ReservationStatusRepository mockRepository;

    @Mock
    private HotelReservationServiceClient mockHotelReservationServiceClient;

    @Mock
    private CarReservationServiceClient mockCarReservationServiceClient;

    @Captor
    private ArgumentCaptor<ReservationStatus> reservationStatusArgumentCaptor;

    private Reservation reservation;
    private ReservationStatus reservationStatus;
    private HotelReservationResponse hotelReservationResponse;
    private CarReservationResponse carReservationResponse;

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

        hotelReservationResponse = new HotelReservationResponse();
        hotelReservationResponse.setId(4L);
        hotelReservationResponse.setReservationId(1L);
        hotelReservationResponse.setStatus(StatusEnum.RESERVED);
        hotelReservationResponse.setHotelId(1L);
        hotelReservationResponse.setHotelName("Holiday Inn");
        hotelReservationResponse.setRoom(666);
        hotelReservationResponse
                .setCheckinDate(new SimpleDateFormat("dd MMM yyyy").parse(HOTEL_CHECKIN_DATE).toString());
        hotelReservationResponse
                .setCheckoutDate(new SimpleDateFormat("dd MMM yyyy").parse(HOTEL_CHECKOUT_DATE).toString());

        carReservationResponse = new CarReservationResponse();
        carReservationResponse.setId(4L);
        carReservationResponse.setStatus(StatusEnum.RESERVED);
        carReservationResponse.setReservationId(1L);
        carReservationResponse.setCarId(1L);
        carReservationResponse.setCarMake("Ford");
        carReservationResponse.setCarModel("Model-T");
        carReservationResponse.setAgency("Hertz");
        carReservationResponse
                .setCheckinDate(new SimpleDateFormat("dd MMM yyyy").parse(HOTEL_CHECKIN_DATE).toString());
        carReservationResponse
                .setCheckoutDate(new SimpleDateFormat("dd MMM yyyy").parse(HOTEL_CHECKOUT_DATE).toString());
    }

    @Test
    void takeNextStepChecksToSeeIfReservationHasReservationStatus() {
        when(mockRepository.findByReservationId(anyLong()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(reservationStatus));
        when(mockHotelReservationServiceClient.makeReservation(any(HotelReservation.class)))
                .thenReturn(hotelReservationResponse);
        underTest.handleReservation(reservation);
        verify(mockRepository, atLeastOnce()).findByReservationId(anyLong());
    }

    @Test
    void takeNextStepCallsHotelReservationClient() {
        when(mockRepository.findByReservationId(anyLong()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(reservationStatus));
        when(mockHotelReservationServiceClient.makeReservation(any(HotelReservation.class)))
                .thenReturn(hotelReservationResponse);
        underTest.handleReservation(reservation);
        verify(mockHotelReservationServiceClient, atLeastOnce()).makeReservation(any(HotelReservation.class));
    }

    @Test
    void takeNextStepCallsCarReservationClientAfterHotelReserved() {
        when(mockRepository.findByReservationId(anyLong())).thenReturn(Optional.of(reservationStatus));
        when(mockCarReservationServiceClient.makeReservation(any(CarReservation.class))).thenReturn(carReservationResponse);
        underTest.handleReservation(reservation);
        verify(mockCarReservationServiceClient, atLeastOnce()).makeReservation(any(CarReservation.class));
    }

    @Test
    void setsReservationStatusAsSaveHotelWhenNoReservationStatusFound() {
        when(mockRepository.findByReservationId(anyLong()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(reservationStatus));
        when(mockHotelReservationServiceClient.makeReservation(any(HotelReservation.class)))
                .thenReturn(hotelReservationResponse);
        underTest.handleReservation(reservation);
        assertAll(() -> {
            verify(mockRepository, atLeastOnce()).findByReservationId(anyLong());
            CoordinationStep nextStep = reservationStatus.getCoordinationStep();
            Long reservationIdOfNextStep = reservationStatus.getReservationId();
            assertEquals(CoordinationStep.RESERVE_HOTEL, nextStep);
            assertEquals(ID, reservationIdOfNextStep);
        });
    }
}