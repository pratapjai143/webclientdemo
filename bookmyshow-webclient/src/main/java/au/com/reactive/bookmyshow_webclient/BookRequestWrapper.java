package au.com.reactive.bookmyshow_webclient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookRequestWrapper {
    private List<BookRequest> bookings;
}
