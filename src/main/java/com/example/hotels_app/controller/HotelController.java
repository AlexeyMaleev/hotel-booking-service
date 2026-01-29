package com.example.hotels_app.controller;

import com.example.hotels_app.model.request.CreateHotelRequest;
import com.example.hotels_app.model.request.HotelFilter;
import com.example.hotels_app.model.request.UpdateHotelRequest;
import com.example.hotels_app.model.request.UpsertRatingRequest;
import com.example.hotels_app.model.response.HotelListResponse;
import com.example.hotels_app.model.response.HotelResponse;
import com.example.hotels_app.service.HotelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Hotels",
        description = "Операции с отелями"
)
@RestController
@RequestMapping("/api/v1/hotels")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @Operation(
            summary = "Получить отель по id",
            description = "Возвращает отель по указанному идентификатору." +
                    " Доступно авторизованным пользователям с ролью USER или ADMIN"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Отель найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HotelResponse.class)
                    )
            ),
            @ApiResponse( responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse( responseCode = "404", description = "Отель не найден")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<HotelResponse> getById(
            @Parameter(description = "Id отеля", example = "1")
            @PathVariable Long id){
        return ResponseEntity.ok(hotelService.findById(id));
    }

    @Operation(
            summary = "Создать отель",
            description = "Доступно только пользователям с ролью ADMIN"
    )
    @ApiResponses({
            @ApiResponse( responseCode = "201", description = "Отель создан"),
            @ApiResponse( responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse( responseCode = "401", description = "Ошибка авторизации"),
            @ApiResponse( responseCode = "403", description = "Недостаточно прав")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<HotelResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные нового отеля",
                    required = true
            )
            @Valid @RequestBody CreateHotelRequest request){
        HotelResponse response  = hotelService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Обновить информацию об отеле",
            description = "Доступно только пользователям с ролью ADMIN"
    )
    @ApiResponses({
            @ApiResponse( responseCode = "200", description = "Информация об отеле обновлена"),
            @ApiResponse( responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse( responseCode = "401", description = "Ошибка авторизации"),
            @ApiResponse( responseCode = "403", description = "Недостаточно прав")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<HotelResponse> update(
            @Parameter(description = "Id отеля", example = "1")
            @PathVariable Long id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Новые данные отеля. Можно передать только те поля, которые нужно изменить.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UpdateHotelRequest.class)
                    )
            )
            @Valid @RequestBody UpdateHotelRequest request){
        HotelResponse response = hotelService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Удалить отель по id",
            description = "Удаляет отель по указанному идентификатору." +
                    " Доступно авторизованным пользователям с ролью ADMIN"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Отель удален"
            ),
            @ApiResponse( responseCode = "404", description = "Отель не найден"),
            @ApiResponse( responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse( responseCode = "403", description = "Недостаточно прав")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteById(
            @Parameter(description = "Id отеля", example = "1")
            @PathVariable Long id){
        hotelService.delete(id);
        return  ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Получить список всех отелей",
            description = "Возвращает список  всех отелей" +
                    " Доступно всем авторизованным пользователям с ролями USER или ADMIN"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Возвращен список всех отелей"
            ),
            @ApiResponse( responseCode = "401", description = "Пользователь не авторизован")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<HotelResponse>> getAllHotels(){
        List<HotelResponse> responseList = hotelService.findAll();
        return ResponseEntity.ok()
                .body(responseList);
    }

    @Operation(
            summary = "Изменить рейтинг отеля",
            description = """
        Добавляет новую оценку отелю по шкале от 1 до 5.

        Рейтинг отеля пересчитывается автоматически на основе
        всех ранее выставленных оценок.
        """
    )
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Рейтинг отеля обновлен",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = HotelResponse.class)
                )
        ),
        @ApiResponse(responseCode = "400", description = "Некорректная оценка"),
        @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
        @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
        @ApiResponse(responseCode = "404", description = "Отель не найден")
    })
    @PutMapping("/{id}/rate")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<HotelResponse> updateRate(
            @Parameter(description = "ID отеля", example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Новая оценка отеля",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UpsertRatingRequest.class)
                    )
            )
            @Valid @RequestBody UpsertRatingRequest request) {
            HotelResponse response = hotelService.updateRating(id, request.getNewMark());

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Поиск и фильтрация отелей",
            description = """
        Возвращает список отелей с возможностью фильтрации и пагинации.
        Параметры фильтра передаются через query-параметры.
        """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Список отелей успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HotelListResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Ошибка в параметрах запроса"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<HotelListResponse> filterHotels(
            @ParameterObject
            @Valid HotelFilter filter,

            @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        HotelListResponse response = hotelService.findAll(filter, pageable);

        return ResponseEntity.ok(response);
    }
}
