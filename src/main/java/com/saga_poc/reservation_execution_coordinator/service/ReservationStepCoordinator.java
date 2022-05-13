package com.saga_poc.reservation_execution_coordinator.service;

import com.saga_poc.reservation_execution_coordinator.client.CarReservationServiceClient;
import com.saga_poc.reservation_execution_coordinator.client.HotelReservationServiceClient;
import com.saga_poc.reservation_execution_coordinator.model.CarReservation;
import com.saga_poc.reservation_execution_coordinator.model.CoordinationStep;
import com.saga_poc.reservation_execution_coordinator.model.HotelReservation;
import com.saga_poc.reservation_execution_coordinator.model.HotelReservationResponse;
import com.saga_poc.reservation_execution_coordinator.model.Reservation;
import com.saga_poc.reservation_execution_coordinator.model.ReservationStatus;
import com.saga_poc.reservation_execution_coordinator.model.StatusEnum;
import com.saga_poc.reservation_execution_coordinator.repository.ReservationStatusRepository;
import org.springframework.stereotype.Service;

@Service
public class ReservationStepCoordinator {

    private final ReservationStatusRepository reservationStatusRepository;
    private final HotelReservationServiceClient hotelReservationServiceClient;
    private final CarReservationServiceClient carReservationServiceClient;

    public ReservationStepCoordinator(ReservationStatusRepository reservationStatusRepository,
                                      HotelReservationServiceClient hotelReservationServiceClient,
                                      CarReservationServiceClient carReservationServiceClient) {
        this.reservationStatusRepository = reservationStatusRepository;
        this.hotelReservationServiceClient = hotelReservationServiceClient;
        this.carReservationServiceClient = carReservationServiceClient;
    }

    public void handleReservation(Reservation reservation) {
        ReservationStatus status = getReservationStatus(reservation);
        CoordinationStep step = getNextStep(status);
        status = cacheStep(reservation, step, status);
        if (step == CoordinationStep.RESERVE_HOTEL) {
            HotelReservationResponse hotelReservationResponse = this.hotelReservationServiceClient
                    .makeReservation(createHotelRequest(reservation));
            if (StatusEnum.RESERVED == hotelReservationResponse.getStatus()) {
                cacheStep(reservation, CoordinationStep.RESERVE_HOTEL, status);
                handleReservation(reservation);
            } else {
                cacheStep(reservation, CoordinationStep.CANCEL_RESERVATION, status);
            }
        } else if (step == CoordinationStep.RESERVE_CAR) {
            this.carReservationServiceClient.makeReservation(createCarRequest(reservation));
        }
    }

    private HotelReservation createHotelRequest(Reservation reservation) {
        return HotelReservation.builder()
                .HotelName(reservation.getHotelName())
                .ReservationId(reservation.getId())
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

    private ReservationStatus cacheStep(Reservation reservation, CoordinationStep step, ReservationStatus status) {
        if (null == status) {
            status = new ReservationStatus();
            status.setReservationId(reservation.getId());
        }
        status.setCoordinationStep(step);
        return this.reservationStatusRepository.save(status);
    }

    private CoordinationStep getNextStep(ReservationStatus status) {
        if (status == null) {
            return CoordinationStep.RESERVE_HOTEL;
        } else if (CoordinationStep.RESERVE_HOTEL == status.getCoordinationStep()) {
            return CoordinationStep.RESERVE_CAR;
        } else if (CoordinationStep.RESERVE_CAR == status.getCoordinationStep()) {
            return CoordinationStep.RESERVE_FLIGHT;
        } else {
            return CoordinationStep.FINALIZE_RESERVATION;
        }
    }

    private ReservationStatus getReservationStatus(Reservation reservation) {
        return this.reservationStatusRepository.findByReservationId(reservation.getId())
                .orElse(null);
    }
}
