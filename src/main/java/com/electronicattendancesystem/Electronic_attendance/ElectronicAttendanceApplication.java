package com.electronicattendancesystem.Electronic_attendance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ElectronicAttendanceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ElectronicAttendanceApplication.class, args);
	}

}
