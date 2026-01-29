package com.example.hotels_app.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Информация о бронировании")
public class BookingResponse {

    @Schema(description = "ID бронирования", example = "1")
    private Long id;

    @Schema(description = "Дата заезда", example = "2026-02-01")
    private LocalDate arrival;

    @Schema(description = "Дата выезда", example = "2026-02-10")
    private LocalDate departure;

    @Schema(description = "Имя забронировавшего пользователя", example = "ivan_ivanov")
    private String userName;

    @Schema(description = "ID забронированной комнаты", example = "10")
    private Long roomId;

    @Schema(description = "Номер комнаты", example = "101")
    private Long roomNumber;

    @Schema(description = "ID отеля", example = "1")
    private Long hotelId;

    @Schema(description = "Название отеля", example = "Grand Palace")
    private String hotelName;
}
