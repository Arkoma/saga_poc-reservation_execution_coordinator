package com.saga_poc.reservation_execution_coordinator.model;

import lombok.Builder;
import lombok.Getter;

import java.util.Date;

@Builder
@Getter
public class CarReservation {
    private String carMake;
    private String carModel;
    private Long reservationId;
    private String agency;
    private Date checkinDate;
    private Date checkoutDate;
}
