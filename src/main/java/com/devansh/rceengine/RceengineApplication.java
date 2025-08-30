package com.devansh.rceengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RceengineApplication {

	public static void main(String[] args) {
		SpringApplication.run(RceengineApplication.class, args);
	}

}
