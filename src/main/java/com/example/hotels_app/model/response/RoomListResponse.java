package com.example.hotels_app.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Ответ со списком комнат и информацией для пагинации")
public class RoomListResponse {

    @Schema(description = "Список найденных комнат")
    private List<RoomResponse> rooms;

    @Schema(description = "Общее количество комнат, соответствующих фильтру", example = "25")
    private Long totalCount;
}
