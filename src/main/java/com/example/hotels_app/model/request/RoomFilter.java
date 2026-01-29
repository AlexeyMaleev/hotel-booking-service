package com.example.hotels_app.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Фильтр для поиска комнат")
public class RoomFilter {

    @Schema(description = "ID комнаты", example = "1")
    private Long id;

    @Schema(description = "Название комнаты (частичное совпадение)", example = "Deluxe")
    private String name;

    @Schema(description = "Минимальная цена", example = "1000.00")
    private BigDecimal minPrice;

    @Schema(description = "Максимальная цена", example = "5000.00")
    private BigDecimal maxPrice;

    @Schema(description = "Вместимость (количество человек)", example = "2")
    private Integer capacity;

    @Schema(description = "Дата заезда", example = "2026-05-01")
    private LocalDate arrival;

    @Schema(description = "Дата выезда", example = "2026-05-10")
    private LocalDate departure;

    @Schema(description = "ID отеля", example = "1")
    private Long  hotelId;
}
