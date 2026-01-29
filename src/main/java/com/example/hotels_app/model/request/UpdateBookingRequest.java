package com.example.hotels_app.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Запрос на обновление данных бронирования")
public class UpdateBookingRequest {

    @Schema(description = "Новая дата заезда", example = "2026-03-01")
    @FutureOrPresent(message = "Дата заезда не может быть в прошлом")
    private LocalDate arrival;

    @Schema(description = "Новая дата выезда", example = "2026-03-05")
    @FutureOrPresent(message = "Дата выезда не может быть в прошлом")
    private LocalDate departure;

    @Schema(description = "ID новой комнаты", example = "2")
    @Positive
    private Long roomId;
}
