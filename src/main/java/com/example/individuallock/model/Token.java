package com.example.individuallock.model;

import java.util.concurrent.locks.ReentrantLock;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Token {

	private ReentrantLock lock;
	private long lastUpdateTimestamp;
	private String token;

}
