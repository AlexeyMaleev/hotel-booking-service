package com.example.hotels_app.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Информация о комнате")
public class RoomResponse {

    @Schema(description = "ID комнаты", example = "1")
    private Long id;

    @Schema(description = "Название комнаты", example = "Стандарт двухместный")
    private String name;

    @Schema(description = "Описание удобств", example = "Кондиционер, Wi-Fi, двуспальная кровать")
    private String description;

    @Schema(description = "Номер комнаты", example = "101")
    private Integer number;

    @Schema(description = "Цена за сутки", example = "4500.00")
    private BigDecimal price;

    @Schema(description = "Вместимость (чел.)", example = "2")
    private Integer capacity;

    @Schema(description = "ID отеля, к которому относится комната", example = "1")
    private Long hotelId;
}
