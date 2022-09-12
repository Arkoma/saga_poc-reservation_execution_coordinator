package com.saga_poc.reservation_execution_coordinator.model;

import com.saga_poc.reservation_execution_coordinator.client.HotelReservationServiceClient;
import com.saga_poc.reservation_execution_coordinator.service.ReservationStepCoordinator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import static com.saga_poc.reservation_execution_coordinator.model.CoordinationStep.RESERVE_CAR;

@Component
public class HotelPlan extends TravelPlan {

    private final HotelReservationServiceClient hotelReservationServiceClient;


    public HotelPlan (@Qualifier("com.saga_poc.reservation_execution_coordinator.client.HotelReservationServiceClient") HotelReservationServiceClient hotelReservationServiceClient,
                      ReservationStepCoordinator reservationStepCoordinator) {
        super(reservationStepCoordinator);
        this.hotelReservationServiceClient = hotelReservationServiceClient;
    }
    @Override
    public void reserve(Reservation reservation, ReservationStatus status) {
        final HotelReservationResponse hotelReservationResponse = this.hotelReservationServiceClient.makeReservation(createHotelRequest(reservation));
//        reservation.setHotelReservationId(hotelReservationResponse.getId());
        status.setCoordinationStep(RESERVE_CAR);
        this.reservationStepCoordinator.handleReservation(reservation, status);
    }

    private HotelReservation createHotelRequest(Reservation reservation) {
        return HotelReservation.builder()
                .hotelName(reservation.getHotelName())
                .reservationId(reservation.getId())
                .room(reservation.getRoom())
                .checkinDate(reservation.getHotelCheckinDate().getTime())
                .checkoutDate(reservation.getHotelCheckoutDate().getTime())
                .build();
    }
}
