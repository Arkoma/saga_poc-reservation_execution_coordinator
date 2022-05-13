package com.saga_poc.reservation_execution_coordinator.client;

import com.saga_poc.reservation_execution_coordinator.model.CarReservation;
import com.saga_poc.reservation_execution_coordinator.model.CarReservationResponse;
import com.saga_poc.reservation_execution_coordinator.model.Endpoints;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(value = "carReservationService", url = Endpoints.BASE_URL + Endpoints.CAR_API_PORT + "/")
public interface CarReservationServiceClient {

    @RequestMapping(method = RequestMethod.POST, value = "/reservation")
    CarReservationResponse makeReservation(CarReservation carReservation);
}
