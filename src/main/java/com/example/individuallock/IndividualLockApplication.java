package com.example.individuallock;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.example.individuallock.service.TokenService;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class IndividualLockApplication {

	@Autowired
	private TokenService tokenService;

	public static void main(String[] args) {
		SpringApplication.run(IndividualLockApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {
			ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

			for (int i = 0; i < 20; i++) {
				executor.submit(() -> {
					String token = tokenService.getToken("KEY1");
					log.info("token: {}", token);
					return null;
				});
			}

			log.info("before waiting");
			Thread.sleep(TokenService.EXPIRITY_MILLIS + 501);
			log.info("after waiting");

			for (int i = 0; i < 20; i++) {
				executor.submit(() -> {
					String token = tokenService.getToken("KEY1");
					log.info("token: {}", token);
					return null;
				});
			}

			executor.awaitTermination(1, TimeUnit.HOURS);
		};
	}

}
