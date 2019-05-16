package com.example.producer;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class ReservationPojoTest {

	@Test
	public void create() throws Exception {
		Reservation reservation = new Reservation("1", "Jane");
		Assert.assertEquals(reservation.getId(), "1");
		Assert.assertEquals(reservation.getName(), "Jane");
		Assert.assertThat(reservation.getName(), Matchers.notNullValue());
		Assert.assertThat(reservation.getId(), Matchers.equalToIgnoringCase("1"));
	}
}
