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
@Schema(description = "Данные для частичного обновления комнаты")
public class UpdateRoomRequest {

    @Schema(description = "Название комнаты", example = "Superior Double")
    private String name;

    @Schema(description = "Описание комнаты", example = "Обновленный интерьер, вид на парк")
    private String description;

    @Schema(description = "Номер комнаты", example = "102")
    @Positive
    private Integer number;

    @Schema(description = "Цена за сутки", example = "6000.00")
    @Positive
    private BigDecimal price;

    @Schema(description = "Максимальная вместимость (чел.)", example = "3")
    @Positive
    private Integer capacity;

    @Schema(description = "ID отеля (если нужно перенести комнату)", example = "1")
    @Positive
    private Long hotelId;
}
