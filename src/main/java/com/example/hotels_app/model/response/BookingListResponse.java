package com.example.hotels_app.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Ответ со списком бронирований и общим количеством записей")
public class BookingListResponse {

    @Schema(description = "Список бронирований")
    private List<BookingResponse> bookings;

    @Schema(description = "Общее количество найденных бронирований", example = "42")
    private Long totalCount;
}
