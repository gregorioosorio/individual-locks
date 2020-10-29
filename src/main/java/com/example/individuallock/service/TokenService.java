package com.example.individuallock.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Service;

import com.example.individuallock.model.Token;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TokenService {

	public static final long EXPIRITY_MILLIS = 1000l;
	public static final long SLEEP_MILLIS = 500l;

	private ConcurrentMap<String, Token> tokens = new ConcurrentHashMap<>();

	public String getToken(String key) {
		Token t = tokens.get(key);

		if (t == null) {
			t = createToken(key);
		}

		try {
			t.getLock().lock();
			log.info("lock");

			if (expired(t.getLastUpdateTimestamp())) {
				renew(key, t);
			}

		} finally {
			log.info("unlock");
			t.getLock().unlock();
		}

		return t.getToken();
	}

	private boolean expired(long lastUpdate) {
		long now = System.currentTimeMillis();
		long elapsed = now - lastUpdate;

		return elapsed >= EXPIRITY_MILLIS;
	}

	synchronized private Token createToken(String key) {
		
		log.info("starting creating first token: {}", key);

		Token t = tokens.get(key);

		if (t == null) {
			log.info("instantiating token: {}", key);
			t = new Token();
			t.setToken("CREATED-EXPIRED");
			t.setLastUpdateTimestamp(System.currentTimeMillis() - EXPIRITY_MILLIS); // always create but expired (to be faster)
			t.setLock(new ReentrantLock());
			tokens.put(key, t);
		}

		log.info("finishing creating first token: {}", key);
		return t;

	}

	private void renew(String key, Token t) {
		
		log.info("starting renewing token: {}", key);
		
		try {
			Thread.sleep(SLEEP_MILLIS);

			t.setToken(t.getToken() + "-UPDATED");
			t.setLastUpdateTimestamp(System.currentTimeMillis());
		} catch (InterruptedException e) {
			log.error("IE: {}", e);
		}
		
		log.info("finishing renewing token: {}", key);
	}

}
