package au.com.reactive.bookmyshow_webclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.sleuth.TraceContext;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;

@SpringBootApplication
@RestController

@RequestMapping("/bookMyShow-client")
public class BookmyshowWebclientApplication {

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	private Tracer tracer;

	public static void main(String[] args) {
		SpringApplication.run(BookmyshowWebclientApplication.class, args);
	}

	Logger log = LoggerFactory.getLogger(BookmyshowWebclientApplication.class);

	@Autowired
	WebClient webClient;

	@PostMapping("/bookNow")
	public Mono<String> BookNow(@RequestBody BookRequest request) {
		return webClient.post().uri("/bookingShow").syncBody(request).retrieve().bodyToMono(String.class);
	}

	@PostMapping("/bookShowInBulk")
	public Mono<List<String>> BookShowInBulk(@RequestBody BookRequestWrapper request) {
		Flux<BookRequest> bookRequestFlux = Flux.fromIterable(request.getBookings());

		return bookRequestFlux.flatMap(booking ->
				getStringMono(booking)
		).collectList();
	}

	@PostMapping("/bookShowInBulkRest")
	public Mono<List<String>> BookShowInBulkRest(@RequestBody BookRequestWrapper request) {
		for (BookRequest booking : request.getBookings()) {
			MDC.put("correlation-id", UUID.randomUUID().toString());
			String response = restTemplate.postForObject("http://localhost:9090/BookMyShow/Service/bookingShow", booking, String.class);
			log.info("Response returned from provider service {}", response);
			MDC.remove("correlation-id");
		}
		return null;
	}

	private Mono<String> getStringMono(BookRequest booking) {

		return webClient.post().uri("/bookingShow").syncBody(booking).retrieve()
				.bodyToMono(String.class);

		/*MDC.put("correlation-id", UUID.randomUUID().toString());
		Mono<String> bookingMono =  webClient.post().uri("/bookingShow").syncBody(booking).retrieve()
				.bodyToMono(String.class);

		MDC.remove("correlation-id");

		return bookingMono;*/
		/*var span = tracer.spanBuilder().setNoParent().start();
		return webClient.post().uri("/bookingShow").syncBody(booking).retrieve().bodyToMono(String.class)
				.contextWrite(Context.of(TraceContext.class, span.context()));*/
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


}
