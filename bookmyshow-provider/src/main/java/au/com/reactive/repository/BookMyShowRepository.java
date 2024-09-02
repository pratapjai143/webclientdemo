package au.com.reactive.repository;


import au.com.reactive.model.BookRequest;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class BookMyShowRepository {

    private List<BookRequest> bookRequestList = new ArrayList<>();

    public List<BookRequest> findAll(){
        return bookRequestList;
    }

    public BookRequest save(BookRequest bookRequest){
        bookRequestList.add(bookRequest);
        return bookRequest;
    }

    public String deleteById(int bookingId){
        return String.valueOf(bookingId);
    }

    public  BookRequest findById(int bookingId){
        return bookRequestList.stream()
                .filter(e -> e.getBookingId() == e.getBookingId())
                .findFirst().get();
    }
}
