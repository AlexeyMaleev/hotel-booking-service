package com.example.hotels_app.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Запрос на создание комнаты")
public class CreateRoomRequest {

    @Schema(description = "Название комнаты", example = "Улучшенный стандарт")
    @NotBlank
    private String name;

    @Schema(description = "Описание комнаты", example = "Вид на море, панорамные окна")
    private String description;

    @Schema(description = "Номер комнаты", example = "101")
    @NotNull
    private Integer number;

    @Schema(description = "Цена за сутки руб.", example = "5500.00")
    @NotNull
    @Positive
    private BigDecimal price;

    @Schema(description = "Максимальная вместимость (чел.)", example = "2")
    @NotNull
    @Positive
    private Integer capacity;

    @Schema(description = "ID отеля, к которому принадлежит комната", example = "1")
    @NotNull
    private Long hotelId;

}
