package com.saga_poc.reservation_execution_coordinator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saga_poc.reservation_execution_coordinator.model.CoordinationStep;
import com.saga_poc.reservation_execution_coordinator.model.HotelReservation;
import com.saga_poc.reservation_execution_coordinator.model.HotelReservationResponse;
import com.saga_poc.reservation_execution_coordinator.model.Reservation;
import com.saga_poc.reservation_execution_coordinator.model.ReservationStatus;
import com.saga_poc.reservation_execution_coordinator.model.StatusEnum;
import com.saga_poc.reservation_execution_coordinator.repository.ReservationStatusRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

import static java.net.http.HttpResponse.BodyHandlers;

@Service
public class ReservationStepCoordinator {

    public static final String API_BASE_URI="http://localhost:";
    public static final String HOTEL_API_PORT="8083";

    private final ReservationStatusRepository reservationStatusRepository;

    public ReservationStepCoordinator(ReservationStatusRepository reservationStatusRepository) {
        this.reservationStatusRepository = reservationStatusRepository;
    }

    public void handleReservation(Reservation reservation) throws URISyntaxException, IOException, InterruptedException {
        ReservationStatus status = getReservationStatus(reservation);
        CoordinationStep step = getNextStep(status);
        status = cacheStep(reservation, step, status);
        if (step == CoordinationStep.RESERVE_HOTEL) {
            final String requestJson = Objects.requireNonNull(createRequest(reservation, step));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(API_BASE_URI + HOTEL_API_PORT + "/reservation"))
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, BodyHandlers.ofString());
            HotelReservationResponse hotelReservationResponse = getResponse(response, HotelReservationResponse.class);
            if (StatusEnum.RESERVED == hotelReservationResponse.getStatus()) {
                cacheStep(reservation, CoordinationStep.RESERVE_HOTEL, status);
                handleReservation(reservation);
            } else {
                cacheStep(reservation, CoordinationStep.CANCEL_RESERVATION, status);
            }
        }
    }

    private String createRequest
            (Reservation reservation, CoordinationStep step) throws JsonProcessingException {
        if (step == CoordinationStep.RESERVE_HOTEL) {
            final HotelReservation hotelReservation = HotelReservation.builder()
                    .HotelName(reservation.getHotelName())
                    .ReservationId(reservation.getId())
                    .room(reservation.getRoom())
                    .checkinDate(reservation.getHotelCheckinDate().getTime())
                    .checkoutDate(reservation.getHotelCheckoutDate().getTime())
                    .build();
            return new ObjectMapper().writeValueAsString(hotelReservation);
        }
        return null;
    }

    private <T extends HotelReservationResponse> T getResponse(HttpResponse<String> response, Class<T> type) throws JsonProcessingException {
        return new ObjectMapper().readValue(response.body(), type);
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
        } else if (status.getCoordinationStep() == CoordinationStep.RESERVE_HOTEL) {
            return CoordinationStep.RESERVE_CAR;
        } else {
            return CoordinationStep.FINALIZE_RESERVATION;
        }
    }

    private ReservationStatus getReservationStatus(Reservation reservation) {
        return this.reservationStatusRepository.findByReservationId(reservation.getId())
                .orElse(null);
    }
}
