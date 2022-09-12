package com.saga_poc.reservation_execution_coordinator.model;

import com.saga_poc.reservation_execution_coordinator.client.HotelReservationServiceClient;
import com.saga_poc.reservation_execution_coordinator.service.ReservationStepCoordinator;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class HotelPlanTest {

    @InjectMocks
    HotelPlan underTest;

    @Mock
    HotelReservationServiceClient mockClient;
    @Mock
    ReservationStepCoordinator mockReservationStepCoordinator;

    @Autowired
    ApplicationContext ac;

    @Autowired
    ReservationStepCoordinator reservationStepCoordinator;

    @Captor
    ArgumentCaptor<HotelReservation> hotelReservationArgumentCaptor;

    @Qualifier("com.saga_poc.reservation_execution_coordinator.client.HotelReservationServiceClient")
    @Autowired
    HotelReservationServiceClient hotelReservationServiceClient;

    @Test
    void hotelPlanIsABean() {
        assertTrue(ac.containsBean("hotelPlan"));
    }

    @Test
    void injectsExpectedDependencies() {
        assertSame(ReflectionTestUtils.getField(underTest, "reservationStepCoordinator"), reservationStepCoordinator);
        assertSame(ReflectionTestUtils.getField(underTest, "hotelReservationServiceClient"), hotelReservationServiceClient);
    }

    @Ignore("WIP")
    @Test
    void reserveCallsClientWithHotelReservationRequest() {
        Reservation reservation = Reservation.builder()
                .id(1L)
                .hotelCheckinDate(new Date())
                .hotelCheckoutDate(new Date()).build();
        ReservationStatus mockStatus = mock(ReservationStatus.class);
        when(mockClient.makeReservation(any(HotelReservation.class))).thenReturn(mock(HotelReservationResponse.class));
        underTest.reserve(reservation, mockStatus);
        verify(mockClient).makeReservation(hotelReservationArgumentCaptor.capture());
        long usedReservationId = hotelReservationArgumentCaptor.getValue().getReservationId();
        assertEquals(1L, usedReservationId);

    }
}