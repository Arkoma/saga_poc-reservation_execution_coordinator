package com.saga_poc.reservation_execution_coordinator.client;

import com.saga_poc.reservation_execution_coordinator.model.Endpoints;
import com.saga_poc.reservation_execution_coordinator.model.HotelReservation;
import com.saga_poc.reservation_execution_coordinator.model.HotelReservationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@FeignClient(value = "hotelReservationService", url = Endpoints.BASE_URL + Endpoints.HOTEL_API_PORT + "/")
public interface HotelReservationServiceClient {

    @RequestMapping(method = POST, value = "/reservation")
    HotelReservationResponse makeReservation(HotelReservation hotelReservation);

    @RequestMapping(method = DELETE, value = "/reservation/{id}")
    void cancelReservation(@PathVariable Long id);
}
