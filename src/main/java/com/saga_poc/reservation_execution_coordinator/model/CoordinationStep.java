package com.saga_poc.reservation_execution_coordinator.model;

public enum CoordinationStep {
    RESERVE_HOTEL,
    RESERVE_CAR,
    RESERVE_FLIGHT,
    CANCEL_HOTEL,
    CANCEL_CAR,
    FINALIZE_RESERVATION,
    CANCEL_RESERVATION
}
