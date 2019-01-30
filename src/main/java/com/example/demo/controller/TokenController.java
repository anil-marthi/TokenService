package com.example.demo.controller;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.config.TokenProperties;


/***
 * Token Web Service (SWE2 or Sr) (In Person) Build a professional grade java web-service that serves the following. 
 * The web service should generate a unique token and give it when a client requests it by calling the API/URI 
 * Once a client acquires the token, it should be valid for 60 seconds. There is no need to keep track of the order of the client requests. 
 * The first client that makes the call gets it, the rest will get the response as described in requirement 4 below until another token is available for consumption. 
 * Any request by another client or the one that already acquired the token, during this 60 seconds should receive a response text “WAIT FOR NEXT TOKEN TO BE AVAILABLE” 
 * A new token should be generated automatically after 60 seconds so that it can service another client 
 * A client that acquired the token should be able to release the token before 60 seconds, the max time the token is valid, by calling another API/URI of the web-service. 
 * If a token is released earlier than 60, then any subsequent call from any client that makes the first call should get a new token and the all the above requirements apply 
 * Lastly the 60 second timeout(time to live) for the token should be configurable. 
 * NOTE : it would be good to have the appropriate unit tests and functional tests added 
 * You have the choice to use any Java Webservice framework(Spring, Jersey etc..) to build this. You have the freedom to design the restful APIs as you deem fit. 
 * If you are not familiar with enterprise Java Web-services programming and yet opt to learn and implement, then it is a bonus. 
 * However, if you cannot do that for some reason, the above problem should still be solved using Core Java features using threads/locks and synchronization etc. 
 * 
 * @author anilm
 *
 */
@RestController
@RequestMapping(path="/TokenService")
public class TokenController {

	@Autowired
	ThreadPoolTaskScheduler taskScheduler;
	
	@Autowired
	TokenProperties tokenProps;
	
	private AtomicReference<String> token;
	private AtomicLong tokenTimestamp;
    private AtomicBoolean tokenAcquired = new AtomicBoolean(false);
	private Runnable runnable;
	private ScheduledFuture<?> future;
	
	@PostConstruct
	public void initialize() {
		runnable = () -> {
			createToken();
			};
		future = taskScheduler.scheduleWithFixedDelay(runnable, new Date(), tokenProps.getDelay());
	}
	
	private void createToken() {
		token = new AtomicReference<>(UUID.randomUUID().toString());
		tokenTimestamp = new AtomicLong(System.currentTimeMillis());
		tokenAcquired = new AtomicBoolean(false);
	}
	
	@RequestMapping(path="/fetchToken", method=RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<String> fetchToken() {
		if(tokenAcquired.get()) {
			return new ResponseEntity<>("WAIT FOR NEXT TOKEN TO BE AVAILABLE", HttpStatus.BAD_REQUEST);
		} else {
			tokenAcquired.set(true);
			return ResponseEntity.ok("Token is " + token.get());
		}
	}
	
	@RequestMapping(path="/releaseToken", method=RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<String> releaseToken(@RequestParam String releaseToken) {
		long currentTimestamp = System.currentTimeMillis();
		if(token.get().equals(releaseToken) && tokenAcquired.get() && (currentTimestamp-tokenTimestamp.get())<tokenProps.getDelay()) {
			future.cancel(true);
			future = taskScheduler.scheduleWithFixedDelay(runnable, new Date(), tokenProps.getDelay());
			return new ResponseEntity<>("Token has been released!", HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Wrong token! The token either has expired or has not been acquired", HttpStatus.BAD_REQUEST);
		}
	}
	
}
