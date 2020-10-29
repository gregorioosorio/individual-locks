package com.example.individuallock.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TokenServiceTest {

	@Autowired
	private TokenService tokenService;

	@Test
	void test_singletoken() {
		String token = tokenService.getToken("KEY1");
		assertEquals("CREATED-EXPIRED-UPDATED", token);
	}

	@Test
	void test_firsttoken() throws InterruptedException, ExecutionException {
		String key = "KEY2";

		ExecutorService executorService = null;
		List<Future<String>> futures = new ArrayList<>();
		try {

			executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
			for (int i = 0; i < 20; i++) {
				futures.add(executorService.submit(() -> {
					return tokenService.getToken(key);
				}));
			}

		} finally {
			if (executorService != null) {
				executorService.shutdown();
			}
		}

		if (executorService != null) {
			executorService.awaitTermination(TokenService.SLEEP_MILLIS * 2, TimeUnit.MILLISECONDS);
			if (!executorService.isTerminated()) {
				fail();
			} else {
				for (Future<String> f : futures) {
					assertEquals("CREATED-EXPIRED-UPDATED", f.get());
				}

			}
		} else {
			fail();
		}

	}

	@Test
	void test_expiredtokens() throws InterruptedException, ExecutionException {
		String key = "KEY3";

		String firstToken = tokenService.getToken(key);
		assertEquals("CREATED-EXPIRED-UPDATED", firstToken);

		Thread.sleep(TokenService.EXPIRITY_MILLIS);

		ExecutorService executorService = null;
		List<Future<String>> futures = new ArrayList<>();
		try {

			executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
			for (int i = 0; i < 20; i++) {
				futures.add(executorService.submit(() -> {
					return tokenService.getToken(key);
				}));
			}

		} finally {
			if (executorService != null) {
				executorService.shutdown();
			}
		}

		if (executorService != null) {
			executorService.awaitTermination(TokenService.SLEEP_MILLIS * 2, TimeUnit.MILLISECONDS);
			if (!executorService.isTerminated()) {
				fail();
			} else {
				for (Future<String> f : futures) {
					assertEquals("CREATED-EXPIRED-UPDATED-UPDATED", f.get());
				}

			}
		} else {
			fail();
		}
	}

}
