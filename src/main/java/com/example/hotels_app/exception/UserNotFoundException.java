package com.example.hotels_app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id){
        super(String.format("User with id %s not found", id));
    }

    public UserNotFoundException(String name){
        super(String.format("User with name: %s not found", name));
    }
}
