package com.internhsip.Assesment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@EnableTransactionManagement

@SpringBootApplication
@EnableScheduling
public class AssesmentApplication {
	public static void main(String[] args) {
		SpringApplication.run(AssesmentApplication.class, args);
	}
}
