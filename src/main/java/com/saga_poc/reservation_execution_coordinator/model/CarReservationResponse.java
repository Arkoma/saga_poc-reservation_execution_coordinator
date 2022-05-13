package com.saga_poc.reservation_execution_coordinator.model;

import lombok.Data;

@Data
public class CarReservationResponse {
    private Long id;
    private StatusEnum status;
    private Long reservationId;
    private Long carId;
    private String carMake;
    private String carModel;
    private String agency;
    private String checkinDate;
    private String checkoutDate;
}
