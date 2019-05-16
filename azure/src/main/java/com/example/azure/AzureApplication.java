package com.example.azure;

import com.microsoft.azure.servicebus.*;
import com.microsoft.azure.spring.data.cosmosdb.core.mapping.Document;
import com.microsoft.azure.spring.data.cosmosdb.repository.DocumentDbRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.annotation.Id;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@SpringBootApplication
public class AzureApplication {

	public static void main(String[] args) {
		SpringApplication.run(AzureApplication.class, args);
	}
}

@RestController
class GreetingRestController {

	@GetMapping("/greetings/{name}")
	Map<String, Object> greet(@PathVariable String name) {
		return Collections.singletonMap("greeting", "Hello " + name + "!");
	}
}

@Log4j2
@Component
@RequiredArgsConstructor
class ServiceBusDemo {

	private final ITopicClient iTopicClient;
	private final ISubscriptionClient iSubscriptionClient;

	@EventListener(ApplicationReadyEvent.class)
	public void demo() throws Exception {

		this.iSubscriptionClient.registerMessageHandler(new IMessageHandler() {

			@Override
			public CompletableFuture<Void> onMessageAsync(IMessage message) {
				log.info("received message " + new String(message.getBody()) + " with body ID " + message.getMessageId());
				return CompletableFuture.completedFuture(null);
			}

			@Override
			public void notifyException(Throwable exception, ExceptionPhase phase) {
				log.error("eeks!", exception);
			}
		});

		Thread.sleep(1000);

		this.iTopicClient.send(new Message("Hello @ " + Instant.now().toString()));

	}
}


@Log4j2
@Component
class CosmosDbDemo {

	private final ReservationRepository rr;

	CosmosDbDemo(ReservationRepository rr) {
		this.rr = rr;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void demo() throws Exception {

		this.rr.deleteAll();

		Stream.of("A", "B", "C")
			.map(name -> new Reservation(null, name))
			.map(this.rr::save)
			.forEach(log::info);

	}
}

interface ReservationRepository extends DocumentDbRepository<Reservation, String> {
}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "reservations")
class Reservation {
	@Id
	private String id;
	private String name;
}

@Component
@Log4j2
class SqlServerDemo {

	private final JdbcTemplate jdbcTemplate;

	SqlServerDemo(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void demo() throws Exception {
		String query = "select TOP 5  * from SalesLT.Customer ";
		RowMapper<Customer> rowMapper =
			(rs, rowNum) -> new Customer(rs.getLong("customerid"), rs.getString("firstname"), rs.getString("lastname"));
		List<Customer> customerList = this.jdbcTemplate.query(query, rowMapper);
		customerList.forEach(log::info);
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Customer {
		private Long id;
		private String firstName, lastName;
	}
}

