package com.saga_poc.reservation_execution_coordinator.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class HotelReservation {
    private String HotelName;
    private Long ReservationId;
    private int room;
    private long checkinDate;
    private long checkoutDate;
}
