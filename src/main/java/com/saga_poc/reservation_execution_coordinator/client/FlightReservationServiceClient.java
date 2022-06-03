package com.saga_poc.reservation_execution_coordinator.client;

import com.saga_poc.reservation_execution_coordinator.model.Endpoints;
import com.saga_poc.reservation_execution_coordinator.model.FlightReservation;
import com.saga_poc.reservation_execution_coordinator.model.FlightReservationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(value = "flightReservationService",
             url = Endpoints.BASE_URL + Endpoints.FLIGHT_API_PORT + "/",
             fallback = FlightReservationServiceFallback.class)
public interface FlightReservationServiceClient {

    @RequestMapping(method = RequestMethod.POST, value = "/reservation")
    FlightReservationResponse makeReservation(FlightReservation flightReservation);
}
