package com.saga_poc.reservation_execution_coordinator.model;

import lombok.Data;

@Data
public class HotelReservationResponse {
    private Long id;
    private Long reservationId;
    private StatusEnum status;
    private Long hotelId;
    private String hotelName;
    private int room;
    private String checkinDate;
    private String checkoutDate;
}
