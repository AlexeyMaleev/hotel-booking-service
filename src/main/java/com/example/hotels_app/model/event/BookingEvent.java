package com.example.hotels_app.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingEvent {

    private Long userId;

    private LocalDate arrival;

    private LocalDate departure;
}
