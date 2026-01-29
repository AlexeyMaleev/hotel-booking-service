package com.example.hotels_app.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Schema(description = "Данные для создания нового отеля")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateHotelRequest {

    @Schema(description = "Название отеля", example = "Hilton")
    @NotBlank
    private String name;

    @Schema(description = "Заголовок/описание отеля", example = "Отель в центре города")
    @NotBlank
    private String title;

    @Schema(description = "Город", example = "Москва")
    @NotBlank
    private String city;

    @Schema(description = "Адрес отеля", example = "ул. Тверская, 1")
    @NotBlank
    private String address;

    @Schema(description = "Расстояние до центра км", example = "5")
    @NotNull
    @Positive
    private Long distance;
}
