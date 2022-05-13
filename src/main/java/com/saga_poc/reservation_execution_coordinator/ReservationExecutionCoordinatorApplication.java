package com.saga_poc.reservation_execution_coordinator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ReservationExecutionCoordinatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationExecutionCoordinatorApplication.class, args);
	}

}
