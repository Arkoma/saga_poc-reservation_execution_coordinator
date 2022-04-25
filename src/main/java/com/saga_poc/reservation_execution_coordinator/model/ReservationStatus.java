package com.saga_poc.reservation_execution_coordinator.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Getter
@Setter
@ToString
@Entity
public class ReservationStatus {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;
    @lombok.NonNull
    private Long reservationId;
    private CoordinationStep coordinationStep;
}
