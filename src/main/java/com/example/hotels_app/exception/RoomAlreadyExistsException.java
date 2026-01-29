package com.example.hotels_app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class RoomAlreadyExistsException extends RuntimeException {
    public RoomAlreadyExistsException(Long hotelId, Integer  roomNumber) {
        super("Room with number " + roomNumber + " already exists in hotel with ID " + hotelId);
    }
}
