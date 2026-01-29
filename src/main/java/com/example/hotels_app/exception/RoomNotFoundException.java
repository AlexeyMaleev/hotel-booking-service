package com.example.hotels_app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class RoomNotFoundException extends RuntimeException {
  public RoomNotFoundException(Long id){
    super(String.format("Room with id %s not found", id));
  }
}
