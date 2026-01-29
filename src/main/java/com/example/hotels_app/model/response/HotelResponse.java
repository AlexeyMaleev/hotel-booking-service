package com.example.hotels_app.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Полная информация об отеле")
public class HotelResponse {

    @Schema(description = "ID отеля", example = "1")
    private Long id;

    @Schema(description = "Название отеля", example = "Grand Plaza")
    private String name;

    @Schema(description = "Заголовок объявления", example = "Роскошный отдых в центре")
    private String title;

    @Schema(description = "Город", example = "Москва")
    private String city;

    @Schema(description = "Адрес отеля", example = "ул. Тверская, д. 1")
    private String address;

    @Schema(description = "Расстояние от центра города (км)", example = "2")
    private Long distance;

    @Schema(description = "Средний рейтинг на основе оценок", example = "4.5")
    private Double rating;

    @Schema(description = "Общее число поставленных оценок", example = "124")
    private Integer ratingCount;

    @Schema(description = "Список номеров отеля")
    private List<RoomResponse> rooms;
}
