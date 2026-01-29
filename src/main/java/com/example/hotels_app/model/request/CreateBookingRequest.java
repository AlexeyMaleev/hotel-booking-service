package com.example.hotels_app.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Запрос на создание бронирования")
public class CreateBookingRequest {

    @Schema(description = "Дата заезда", example = "2026-02-01")
    @NotNull
    @FutureOrPresent(message = "Дата заезда не может быть в прошлом")
    private LocalDate arrival;

    @Schema(description = "Дата выезда", example = "2026-02-10")
    @NotNull
    @FutureOrPresent(message = "Дата выезда не может быть в прошлом")
    private LocalDate departure;

    @Schema(description = "ID пользователя", example = "1")
    @NotNull
    private Long userId;

    @Schema(description = "ID комнаты", example = "1")
    @NotNull
    private Long roomId;
}
