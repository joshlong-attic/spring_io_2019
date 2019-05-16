package com.example.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureJson
@AutoConfigureStubRunner(
	ids = "com.example:producer:+:8080",
	stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
//@AutoConfigureWireMock(port = 8080)
public class ReservationClientTest {

	@Autowired
	private ReservationClient client;

	@Autowired
	private ObjectMapper objectMapper;

	@SneakyThrows
	private String jsonFrom(Collection<Reservation> reservations) {
		return this.objectMapper.writeValueAsString(reservations);
	}

	@Test
	public void getAllReservations() throws Exception {
		/*
		List<Reservation> reservations = Arrays.asList(new Reservation("1", "Jane"), new Reservation("2", "John"));
		List<String> validNames = reservations.stream().map(r -> r.getReservationName()).collect(Collectors.toList());
		String json = this.jsonFrom(reservations);

		WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/reservations"))
			.willReturn(WireMock.aResponse().withStatus(200)
				.withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.withBody(json)
			));
		*/

		List<String> validNames = Arrays.asList("Jane", "John");

		Flux<Reservation> reservationFlux = this.client.getAllReservations();

		StepVerifier
			.create(reservationFlux)
			.expectNextMatches(reservation -> validNames.contains(reservation.getName()) && reservation.getId() != null)
			.expectNextMatches(reservation -> validNames.contains(reservation.getName()) && reservation.getId() != null)
			.verifyComplete();
	}
}
