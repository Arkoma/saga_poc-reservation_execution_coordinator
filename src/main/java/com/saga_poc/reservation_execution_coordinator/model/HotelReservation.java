package com.saga_poc.reservation_execution_coordinator.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class HotelReservation {
    private Long reservationId;
    private String hotelName;
    private int room;
    private long checkinDate;
    private long checkoutDate;
}
