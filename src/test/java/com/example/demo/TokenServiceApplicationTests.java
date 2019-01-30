package com.example.demo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TokenServiceApplicationTests {

	private RestTemplate restTemplate;

	private static String fetchTokenUrl = "http://localhost:8080/TokenService/fetchToken";
	private static String releaseTokenUrl = "http://localhost:8080/TokenService/releaseToken";

    @Before
    public void beforeTest() {
        restTemplate = new RestTemplate();
    }
    
	@Test
	public void testFetch() {
		ResponseEntity<String> responseEntity = restTemplate.getForEntity(fetchTokenUrl, String.class);
		assert(responseEntity.getStatusCode().equals(HttpStatus.OK));
		System.out.println(responseEntity.getBody());
	}

	@Test
	public void testRelease(String token) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("releaseToken", token);
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
		ResponseEntity<String> response = restTemplate.exchange(releaseTokenUrl, HttpMethod.POST, request,
				String.class);
		assert(response.getStatusCode().equals(HttpStatus.OK));
		System.out.println(response.getBody());
	}

	@Test
	public void testFetchAndRelease() {
		ResponseEntity<String> responseEntity = restTemplate.getForEntity(fetchTokenUrl, String.class);
		assert(responseEntity.getStatusCode().equals(HttpStatus.OK));
		System.out.println(responseEntity.getBody());
		String token = responseEntity.getBody().split(" ")[2];
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("releaseToken", token);
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
		ResponseEntity<String> response = restTemplate.exchange(releaseTokenUrl, HttpMethod.POST, request,
				String.class);
		assert(response.getStatusCode().equals(HttpStatus.OK));
		System.out.println(response.getBody());
	}
}
