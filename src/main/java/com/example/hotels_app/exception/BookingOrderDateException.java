package com.example.hotels_app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BookingOrderDateException extends IllegalArgumentException {
    public BookingOrderDateException(String departure, String arrival){
        super(String.format("Booking departure value: %s before  arrival value: %s",
                departure,
                arrival));
    }
}
