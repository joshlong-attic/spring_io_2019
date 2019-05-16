package com.example.producer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DataMongoTest
@RunWith(SpringRunner.class)
public class ReservationEntityTest {

	@Autowired
	private ReactiveMongoTemplate template;

	@Test
	public void persist() throws Exception {

		Mono<Reservation> jane = this.template.save(new Reservation(null, "Jane"));
		StepVerifier
			.create(jane)
			.expectNextMatches(reservation -> reservation.getName().equalsIgnoreCase("Jane") && reservation.getId() != null)
			.verifyComplete();
	}
}
