package com.saga_poc.reservation_execution_coordinator.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saga_poc.reservation_execution_coordinator.model.Reservation;
import com.saga_poc.reservation_execution_coordinator.model.StatusEnum;
import com.saga_poc.reservation_execution_coordinator.service.ReservationStepCoordinator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReservationStatusConsumerTest {

    public static final long ID = 1L;
    public static final String CUSTOMER_NAME = "Tom Brady";
    public static final String HOTEL_NAME = "Holiday Inn";
    public static final String CAR_MAKE = "Ford";
    public static final String CAR_MODEL = "Model-T";

    @InjectMocks
    private ReservationStatusConsumer underTest;

    @Mock
    private ReservationStepCoordinator reservationStepCoordinator;

    @Captor
    private ArgumentCaptor<Reservation> reservationArgumentCaptor;

    private Reservation reservation;

    @BeforeEach
    void setup() {
        reservation = Reservation.builder()
                .id(ID)
                .customerName(CUSTOMER_NAME)
                .hotelName(HOTEL_NAME)
                .carMake(CAR_MAKE)
                .carModel(CAR_MODEL)
                .status(StatusEnum.PENDING)
                .build();
    }

    @Test
    void receiveCallsStepCoordinatorTakeNextStep() throws IOException {
        String payload = new ObjectMapper().writeValueAsString(reservation);
        underTest.receive(payload);
        verify(reservationStepCoordinator, times(1)).handleReservation(reservationArgumentCaptor.capture());
        Reservation sentReservation = reservationArgumentCaptor.getValue();
        assertAll(() -> {
            assertEquals(ID, sentReservation.getId());
            assertEquals(CUSTOMER_NAME, sentReservation.getCustomerName());
            assertEquals(HOTEL_NAME, sentReservation.getHotelName());
            assertEquals(CAR_MAKE, sentReservation.getCarMake());
            assertEquals(CAR_MODEL, sentReservation.getCarModel());
            assertEquals(StatusEnum.PENDING, sentReservation.getStatus());

        });
    }

}