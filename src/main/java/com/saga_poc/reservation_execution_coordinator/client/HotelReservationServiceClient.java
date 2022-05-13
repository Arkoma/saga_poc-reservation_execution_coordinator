package com.saga_poc.reservation_execution_coordinator.client;

import com.saga_poc.reservation_execution_coordinator.model.Endpoints;
import com.saga_poc.reservation_execution_coordinator.model.HotelReservation;
import com.saga_poc.reservation_execution_coordinator.model.HotelReservationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(value = "hotelReservationService", url = Endpoints.BASE_URL + Endpoints.HOTEL_API_PORT + "/")
public interface HotelReservationServiceClient {

    @RequestMapping(method = RequestMethod.POST, value = "/reservation")
    HotelReservationResponse makeReservation(HotelReservation hotelReservation);
}
