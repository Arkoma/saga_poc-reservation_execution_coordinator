package com.saga_poc.reservation_execution_coordinator.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Reservation {
    private Long id;
    private String customerName;
    private String hotelName;
    private int room;
    private Date hotelCheckinDate;
    private Date hotelCheckoutDate;
    private Long hotelReservationId;
    private String carMake;
    private String carModel;
    private String carAgency;
    private Date carRentalDate;
    private Date carReturnDate;
    private Long carReservationId;
    private String flightNumber;
    private String seatNumber;
    private Date flightDepartureDate;
    private Date flightReturnDate;
    private String flightReservationId;
    private StatusEnum status;
}
