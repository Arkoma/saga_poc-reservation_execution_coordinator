package com.saga_poc.reservation_execution_coordinator.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class FlightReservation {
    private Long reservationId;
    private int flightNumber;
    private String seatNumber;
    private long departureDate;
    private long returnDate;
}
