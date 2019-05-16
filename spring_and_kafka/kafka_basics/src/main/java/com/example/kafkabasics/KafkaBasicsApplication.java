package com.example.kafkabasics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@SpringBootApplication
public class KafkaBasicsApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaBasicsApplication.class, args);
	}

	@Bean
	NewTopic greetingsTopic() {
		return new NewTopic("greetings", 1, (short) 1);
	}
}

@Log4j2
@Component
@RequiredArgsConstructor
class Producer {

	private final KafkaTemplate<String, Greeting> template;

	@EventListener(ApplicationReadyEvent.class)
	public void process() throws Exception {
		IntStream
			.range(0, 10)
			.mapToObj(value -> new Greeting("hello @ " + value))
			.map(g -> this.template.send("greetings", "g" + UUID.randomUUID().toString(), g))
			.forEach(log::info);
	}

}

@Log4j2
@Component
class Consumer {

	@KafkaListener(topics = "greetings")
	public void onNewGreeting(Greeting greeting) {
		log.info("new greeting: " + greeting.toString());
	}
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Greeting {
	private String message;
}