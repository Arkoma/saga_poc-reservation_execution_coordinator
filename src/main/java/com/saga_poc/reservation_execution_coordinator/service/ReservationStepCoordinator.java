package com.saga_poc.reservation_execution_coordinator.service;

import com.saga_poc.reservation_execution_coordinator.client.CarReservationServiceClient;
import com.saga_poc.reservation_execution_coordinator.client.FlightReservationServiceClient;
import com.saga_poc.reservation_execution_coordinator.client.HotelReservationServiceClient;
import com.saga_poc.reservation_execution_coordinator.model.CarReservation;
import com.saga_poc.reservation_execution_coordinator.model.CarReservationResponse;
import com.saga_poc.reservation_execution_coordinator.model.FlightReservation;
import com.saga_poc.reservation_execution_coordinator.model.FlightReservationResponse;
import com.saga_poc.reservation_execution_coordinator.model.HotelReservation;
import com.saga_poc.reservation_execution_coordinator.model.HotelReservationResponse;
import com.saga_poc.reservation_execution_coordinator.model.Reservation;
import com.saga_poc.reservation_execution_coordinator.model.ReservationStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static com.saga_poc.reservation_execution_coordinator.model.CoordinationStep.CANCEL_CAR;
import static com.saga_poc.reservation_execution_coordinator.model.CoordinationStep.CANCEL_HOTEL;
import static com.saga_poc.reservation_execution_coordinator.model.CoordinationStep.RESERVE_CAR;
import static com.saga_poc.reservation_execution_coordinator.model.CoordinationStep.RESERVE_FLIGHT;
import static com.saga_poc.reservation_execution_coordinator.model.CoordinationStep.RESERVE_HOTEL;
import static com.saga_poc.reservation_execution_coordinator.model.StatusEnum.CANCELED;

@Service
public class ReservationStepCoordinator {

    private final HotelReservationServiceClient hotelReservationServiceClient;
    private final CarReservationServiceClient carReservationServiceClient;
    private final FlightReservationServiceClient flightReservationServiceClient;

    public ReservationStepCoordinator(@Qualifier("com.saga_poc.reservation_execution_coordinator.client.HotelReservationServiceClient")
                                      HotelReservationServiceClient hotelReservationServiceClient,
                                      @Qualifier("com.saga_poc.reservation_execution_coordinator.client.CarReservationServiceClient")
                                      CarReservationServiceClient carReservationServiceClient,
                                      @Qualifier("com.saga_poc.reservation_execution_coordinator.client.FlightReservationServiceClient")
                                      FlightReservationServiceClient flightReservationServiceClient) {
        this.hotelReservationServiceClient = hotelReservationServiceClient;
        this.carReservationServiceClient = carReservationServiceClient;
        this.flightReservationServiceClient = flightReservationServiceClient;
    }

    public void handleReservation(Reservation reservation) {
        this.handleReservation(reservation, null);
    }
    private void handleReservation(Reservation reservation, ReservationStatus status) {
        if (status == null) {
            status = new ReservationStatus();
            status.setReservationId(reservation.getId());
            status.setCoordinationStep(RESERVE_HOTEL);
        }
        switch (status.getCoordinationStep()) {
            case RESERVE_HOTEL:
                final HotelReservationResponse hotelReservationResponse = this.hotelReservationServiceClient
                        .makeReservation(createHotelRequest(reservation));
                reservation.setHotelReservationId(hotelReservationResponse.getId());
                status.setCoordinationStep(RESERVE_CAR);
                handleReservation(reservation, status);
                break;
            case RESERVE_CAR:
                final CarReservationResponse carReservationResponse = this.carReservationServiceClient
                        .makeReservation(createCarRequest(reservation));
                status.setCoordinationStep(RESERVE_FLIGHT);
                reservation.setCarReservationId(carReservationResponse.getId());
                handleReservation(reservation, status);
                break;
            case RESERVE_FLIGHT:
                final FlightReservationResponse flightReservationResponse = this.flightReservationServiceClient
                        .makeReservation(createFlightRequest(reservation));
                if (CANCELED == flightReservationResponse.getStatus()) {
                    status.setCoordinationStep(CANCEL_CAR);
                    handleReservation(reservation, status);
                }
            case CANCEL_CAR:
                this.carReservationServiceClient.cancelReservation(reservation.getCarReservationId());
                status.setCoordinationStep(CANCEL_HOTEL);
                handleReservation(reservation, status);
                break;
            case CANCEL_HOTEL:
                this.hotelReservationServiceClient.cancelReservation(reservation.getHotelReservationId());
            default:
                break;
        }
    }

    private FlightReservation createFlightRequest(Reservation reservation) {
        return FlightReservation.builder()
                .reservationId(reservation.getId())
                .flightNumber(Integer.parseInt(reservation.getFlightNumber()))
                .seatNumber(reservation.getSeatNumber())
                .departureDate(reservation.getFlightDepartureDate().getTime())
                .returnDate(reservation.getFlightReturnDate().getTime())
                .build();
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

    private CarReservation createCarRequest(Reservation reservation) {
        return CarReservation.builder()
                .carMake(reservation.getCarMake())
                .carModel(reservation.getCarModel())
                .reservationId(reservation.getId())
                .agency(reservation.getCarAgency())
                .checkinDate(reservation.getCarRentalDate())
                .checkoutDate(reservation.getCarReturnDate())
                .build();
    }

}
