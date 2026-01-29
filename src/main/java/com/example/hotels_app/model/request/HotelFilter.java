package com.example.hotels_app.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Фильтр для поиска отелей")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelFilter {

    @Schema(description = "Идентификатор отеля", example = "7")
    private Long id;

    @Schema(description = "Название отеля", example = "Hilton")
    private String name;

    @Schema(description = "Заголовок/описание отеля", example = "Отель в центре города")
    private String title;

    @Schema(description = "Город", example = "Москва")
    private String city;

    @Schema(description = "Адрес отеля", example = "ул. Маяковского 19")
    private String address;

    @Schema(description = "Расстояние до центра в километрах", example = "3")
    private Long distance;

    @Schema(description = "Рейтинг отеля", example = "3.7")
    private Double rating;

    @Schema(description = "Число оценок рейтинга отеля", example = "7")
    private Integer ratingCount;
}
