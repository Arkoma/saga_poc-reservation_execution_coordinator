package com.saga_poc.reservation_execution_coordinator.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Getter
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
