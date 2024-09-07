package au.com.reactive.bookmyshow_webclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.List;

@SpringBootApplication
@RestController
@RequestMapping("/bookMyShow-client")
public class BookmyshowWebclientApplication {

	@Autowired
	WebClient.Builder webClientBuilder;

	@Autowired
	private Tracer tracer;

	public static void main(String[] args) {
		SpringApplication.run(BookmyshowWebclientApplication.class, args);
	}

	Logger log = LoggerFactory.getLogger(BookmyshowWebclientApplication.class);

	WebClient webClient;

	@PostConstruct
	public void init() {
		webClient = webClientBuilder.baseUrl("http://localhost:9090/BookMyShow/Service")
		//.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.filter(logRequest())
		.filter(logResponse()).build();
	}

	@PostMapping("/bookNow")
	public Mono<String> BookNow(@RequestBody BookRequest request) {
		return webClient.post().uri("/bookingShow").syncBody(request).retrieve().bodyToMono(String.class);
	}

	@PostMapping("/bookShowInBulk")
	public Mono<List<String>> BookShowInBulk(@RequestBody BookRequestWrapper request) {
		Flux<BookRequest> bookRequestFlux = Flux.fromIterable(request.getBookings());

		return bookRequestFlux.flatMap(booking ->
				webClient.post().uri("/bookingShow").syncBody(booking).retrieve().bodyToMono(String.class)
		).collectList();
		//return webClient.post().uri("/bookingShow").syncBody(request).retrieve().bodyToMono(String.class);
	}

	@GetMapping("/trackBookings")
	public Flux<BookRequest> trackAllBooking() {
		return webClient.get().uri("/getAllBooking").retrieve().bodyToFlux(BookRequest.class);
	}

	@GetMapping("/trackBooking/{bookingId}")
	public Mono<BookRequest> getBookingById(@PathVariable int bookingId) {
		return webClient.get().uri("/getBooking/" + bookingId).retrieve()
				.onStatus(HttpStatus::is4xxClientError,
						clinetResponse -> Mono.error(new BookMyShowClientException(" 404 unsported exception")))
				.onStatus(HttpStatus::is5xxServerError,
						clinetResponse -> Mono.error(new BookMyShowClientException(" 505 Server exception")))
				.bodyToMono(BookRequest.class);
	}

	@DeleteMapping("/removeBooking/{bookingId}")
	public Mono<String> cancelBooking(@PathVariable int bookingId) {
		return webClient.delete().uri("/cancelBooking/" + bookingId).retrieve().bodyToMono(String.class);
	}

	@PutMapping("/changeBooking/{bookingId}")
	public Mono<BookRequest> updateBooking(@PathVariable int bookingId, @RequestBody BookRequest request) {
		return webClient.put().uri("/updateBooking/" + bookingId).syncBody(request).retrieve()
				.bodyToMono(BookRequest.class);
	}

	private ExchangeFilterFunction logRequest() {
		return ExchangeFilterFunction.ofRequestProcessor(clinetRequest -> {
			log.info("Request {} {}", clinetRequest.method(), clinetRequest.url());
			MDC.put("x-user-id", clinetRequest.headers().get("X-B3-SpanId").stream().findFirst().get());
			clinetRequest.headers().forEach((name, values) -> values.forEach(value -> log.info("{}={}", name, value)));
			return Mono.just(clinetRequest);
		});
	}

	private ExchangeFilterFunction logResponse() {
		return ExchangeFilterFunction.ofResponseProcessor(clinetResponse -> {
			log.info("Response status code {} ", clinetResponse.statusCode());
			return Mono.just(clinetResponse);
		});
	}


}
