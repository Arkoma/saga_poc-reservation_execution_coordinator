package com.saga_poc.reservation_execution_coordinator.model;

import lombok.Data;

@Data
public class FlightReservationResponse {
    private Long id;
    private StatusEnum status;
    private Long reservationId;
    private Long flightId;
    private String flightNumber;
    private String seatNumber;
    private String departureDate;
    private String returnDate;
}
