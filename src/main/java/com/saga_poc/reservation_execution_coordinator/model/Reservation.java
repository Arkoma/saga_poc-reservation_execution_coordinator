package com.saga_poc.reservation_execution_coordinator.model;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Reservation {
    private Long id;
    private String customerName;
    private String hotelName;
    private Long hotelId;
    private String carMake;
    private String carModel;
    private Long carId;
    private String flightNumber;
    private String flightId;
    private StatusEnum status;
}
