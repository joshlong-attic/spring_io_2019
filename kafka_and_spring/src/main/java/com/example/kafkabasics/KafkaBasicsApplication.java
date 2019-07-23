package com.example.kafkabasics;

import io.confluent.demo.Movie;
import io.confluent.demo.Rating;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.kafkabasics.KafkaBasicsApplication.MOVIES_TOPIC;

@RequiredArgsConstructor
@SpringBootApplication
public class KafkaBasicsApplication {

	final static String RATINGS_TOPIC = "ratings";
	final static String MOVIES_TOPIC = "movies";

	public static void main(String[] args) {
		SpringApplication.run(KafkaBasicsApplication.class, args);
	}

	@Bean
	ProducerFactory<Long, Movie> movieProducerFactory() {
		return new DefaultKafkaProducerFactory<>(producerConfigs());
	}

	@Bean
	KafkaTemplate<Long, Movie> movieTemplate() {
		return new KafkaTemplate<>(movieProducerFactory());
	}

	@Bean
	NewTopic ratingsTopic() {
		return new NewTopic(MOVIES_TOPIC, 1, (short) 1);
	}

	@Bean
	NewTopic moviesTopic() {
		return new NewTopic(RATINGS_TOPIC, 1, (short) 1);
	}

	@Bean
	ProducerFactory<Long, Rating> ratingsProducerFactory() {
		return new DefaultKafkaProducerFactory<>(producerConfigs());
	}

	@Bean
	KafkaTemplate<Long, Rating> ratingTemplate() {
		return new KafkaTemplate<>(ratingsProducerFactory());
	}

	@Bean
	Map<String, Object> producerConfigs() {
		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
		props.put("schema.registry.url", "http://localhost:8081");
		return props;
	}

}

@Log4j2
@Component
@RequiredArgsConstructor
class Producer {

	private final KafkaTemplate<Long, Movie> movieTemplate;
	private final KafkaTemplate<Long, Rating> ratingTemplate;

	@Value(value = "classpath:movies.dat")
	private Resource moviesFile;

	@EventListener(ApplicationReadyEvent.class)
	public void process() throws Exception {

		try (Stream<String> stream = Files.lines(Paths.get(moviesFile.getURI()))) {

			stream.forEach(s -> {
				Movie movie = Parser.parseMovie(s);
				log.info("sending " + movie.getMovieId() + " for movie " + movie.toString() + " to " + MOVIES_TOPIC);
				movieTemplate.send(MOVIES_TOPIC, movie.getMovieId(), movie);
			});
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		Random ran = new Random();
		while (true) {
			int movieId = ran.nextInt(920) + 1;
			int rating = 5 + ran.nextInt(6);
			Rating rat = new Rating((long) movieId, (double) rating);
			log.info(rat.toString());
			Thread.sleep(1_000);
			this.ratingTemplate.send(KafkaBasicsApplication.RATINGS_TOPIC, rat.getMovieId(), rat);
		}
	}
}

class Parser {

	private static List<String> parseArray(String text) {
		return Collections.list(new StringTokenizer(text, "|")).stream()
			.map(token -> (String) token)
			.collect(Collectors.toList());
	}

	public static Movie parseMovie(String text) {
		String[] tokens = text.split("\\:\\:");
		String id = tokens[0];
		String title = tokens[1];
		String releaseYear = tokens[2];
		String country = tokens[4];
		String genres = tokens[7];
		String actors = tokens[8];
		String directors = tokens[9];
		String composers = tokens[10];
		String screenwriters = tokens[11];
		String cinematographer = tokens[12];
		String productionCompanies = "";
		if (tokens.length > 13) {
			productionCompanies = tokens[13];
		}

		Movie movie = new Movie();
		movie.setMovieId(Long.parseLong(id));
		movie.setTitle(title);
		movie.setReleaseYear(Integer.parseInt(releaseYear));
		movie.setCountry(country);
		//movie.setRating(Float.parseFloat(rating));
		movie.setGenres(Parser.parseArray(genres));
		movie.setActors(Parser.parseArray(actors));
		movie.setDirectors(Parser.parseArray(directors));
		movie.setComposers(Parser.parseArray(composers));
		movie.setScreenwriters(Parser.parseArray(screenwriters));
		movie.setCinematographer(cinematographer);
		movie.setProductionCompanies(Parser.parseArray(productionCompanies));

		return movie;
	}
}

