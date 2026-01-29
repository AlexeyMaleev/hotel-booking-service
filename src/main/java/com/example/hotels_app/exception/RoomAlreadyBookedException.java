package com.example.hotels_app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.Instant;

@ResponseStatus(HttpStatus.CONFLICT)
public class RoomAlreadyBookedException extends RuntimeException {
    public RoomAlreadyBookedException(Long id, Instant start, Instant end) {
        super(String.format("Room with id: %d already busy in that period: %s - %s",
                id, start.toString(), end.toString()));
    }
}
