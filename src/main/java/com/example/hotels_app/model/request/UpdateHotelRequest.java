package com.example.hotels_app.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Schema(description = "Данные для частичного обновления отеля. " +
        "Передаются только поля, которые нужно изменить.")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateHotelRequest {

    @Schema(description = "Название отеля", example = "Hilton")
    private String name;

    @Schema(description = "Заголовок/описание отеля", example = "Отель в центре города")
    private String title;

    @Schema(description = "Город", example = "Москва")
    private String city;

    @Schema(description = "Адрес", example = "ул. Тверская, 1")
    private String address;

    @Schema(description = "Расстояние до центра км", example = "5")
    @Positive
    private Long distance;
}
