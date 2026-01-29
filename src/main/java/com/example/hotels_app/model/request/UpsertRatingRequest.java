package com.example.hotels_app.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Запрос на добавление новой оценки отелю")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpsertRatingRequest {

    @Schema(
            description = "Новая оценка отеля (от 1 до 5)",
            example = "5",
            minimum = "1",
            maximum = "5"
    )
    @NotNull
    @Min(1)
    @Max(5)
    private Integer newMark;
}
