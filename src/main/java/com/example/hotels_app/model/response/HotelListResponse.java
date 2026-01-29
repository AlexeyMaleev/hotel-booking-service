package com.example.hotels_app.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Ответ со списком отелей и информацией для пагинации")
public class HotelListResponse {

    @Schema(description = "Список отелей")
    private List<HotelResponse> hotels;

    @Schema(description = "Общее количество отелей в базе (для пагинации)", example = "150")
    private Long totalCount;
}
