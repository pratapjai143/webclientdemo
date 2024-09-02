package au.com.reactive;

import au.com.reactive.model.BookRequest;
import au.com.reactive.repository.BookMyShowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SpringBootApplication
@RestController
@RequestMapping("BookMyShow/Service")
public class BookmyshowProviderApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookmyshowProviderApplication.class, args);
	}

	@Autowired
	private BookMyShowRepository repository;

	@PostMapping("/bookingShow")
	public String bookShow(@RequestBody BookRequest bookRequest) {
		BookRequest response = repository.save(bookRequest);
		return "Hi " + response.getUserName() + " your Request for " + response.getShowName() + " on date "
				+ response.getBookingTime() + "Booking successfully..";
	}

	@GetMapping("/getAllBooking")
	public List<BookRequest> getAllBooking() {
		return repository.findAll();
	}

	@GetMapping("/getBooking/{bookingId}")
	public BookRequest getBooking(@PathVariable int bookingId) {
		return repository.findById(bookingId);
	}

	@DeleteMapping("/cancelBooking/{bookingId}")
	public String cancelBooking(@PathVariable int bookingId) {
		repository.deleteById(bookingId);
		return "Booking cancelled with bookingId : " + bookingId;
	}

	@PutMapping("/updateBooking/{bookingId}")
	public BookRequest updateBooking(@RequestBody BookRequest updateBookRequest, @PathVariable int bookingId) {
		BookRequest dbResponse = repository.findById(bookingId);
		dbResponse.setBookingTime(updateBookRequest.getBookingTime());
		dbResponse.setPrice(updateBookRequest.getPrice());
		dbResponse.setShowName(updateBookRequest.getShowName());
		dbResponse.setUserCount(updateBookRequest.getUserCount());
		//repository.saveAndFlush(dbResponse);
		return dbResponse;
	}
}
