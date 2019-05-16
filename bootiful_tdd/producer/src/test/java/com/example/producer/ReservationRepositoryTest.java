package com.example.producer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@DataMongoTest
@RunWith(SpringRunner.class)
public class ReservationRepositoryTest {

	@Autowired
	private ReservationRepository repository;

	@Test
	public void query() throws Exception {

		Flux<Reservation> reservationFlux =
			repository.deleteAll()
				.thenMany(
					Flux.just("A", "B", "C", "C")
						.map(nae -> new Reservation(null, nae))
						.flatMap(name -> this.repository.save(name)))
				.thenMany(this.repository.findByName("C"));

		StepVerifier
			.create(reservationFlux)
			.expectNextCount(2)
			.verifyComplete();


	}
}
