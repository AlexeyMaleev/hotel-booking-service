package com.example.hotels_app.controller;

import com.example.hotels_app.model.request.*;
import com.example.hotels_app.model.response.RoomListResponse;
import com.example.hotels_app.model.response.RoomResponse;
import com.example.hotels_app.service.RoomService;
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
        name = "Rooms",
        description = "Операции с комнатами"
)
@RestController
@RequestMapping("/api/v1/rooms")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @Operation(
            summary = "Получить все комнаты",
            description = "Возвращает список всех комнат" +
                    " Доступно авторизованным пользователям с ролью USER или ADMIN"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Возвращен список всех комнат"
            ),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<RoomResponse>> getAllRooms(){
        List<RoomResponse> responseList = roomService.findAll();
        return ResponseEntity.ok()
                .body(responseList);
    }

    @Operation(
            summary = "Найти комнату по id",
            description = "Возвращает комнату по указанному идентификатору." +
                    " Доступно авторизованным пользователям с ролью USER или ADMIN"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Комната найдена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RoomResponse.class)
                    )
            ),
            @ApiResponse( responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse( responseCode = "404", description = "Комната не найдена")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<RoomResponse> getById(
            @Parameter(description = "Id комнаты", example = "7")
            @PathVariable Long id){
        return ResponseEntity.ok(roomService.findById(id));
    }

    @Operation(
            summary = "Создать комнату",
            description = "Создает новую комнату." +
                    " Доступно только пользователям с ролью ADMIN"
    )
    @ApiResponses({
            @ApiResponse( responseCode = "201", description = "Комната создана"),
            @ApiResponse( responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse( responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse( responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse( responseCode = "409", description = "Комната с таким номером уже существует в данном отеле")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<RoomResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные новой комнаты",
                    required = true
            )
            @Valid @RequestBody CreateRoomRequest request){
        RoomResponse response  = roomService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Обновить информацию о комнате",
            description = "Доступно только пользователям с ролью ADMIN"
    )
    @ApiResponses({
            @ApiResponse( responseCode = "200", description = "Информация о комнате обновлена"),
            @ApiResponse( responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse( responseCode = "401", description = "Ошибка авторизации"),
            @ApiResponse( responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse( responseCode = "409", description = "Комната с таким номером уже существует в данном отеле")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public  ResponseEntity<RoomResponse> update(
            @Parameter(description = "Id комнаты", example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные комнаты для обновления",
                    required = true
            )
            @Valid @RequestBody UpdateRoomRequest request){
        RoomResponse response = roomService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Удалить комнату по id",
            description = "Удаляет комнату по указанному идентификатору." +
                    " Доступно авторизованным пользователям с ролью ADMIN"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Комната удалена"
            ),

            @ApiResponse( responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse( responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse( responseCode = "404", description = "Комната не найдена")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteById(
            @Parameter(description = "Id комнаты", example = "1")
            @PathVariable Long id){
        roomService.delete(id);
        return  ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Поиск и фильтрация комнат",
            description = """
        Возвращает список комнат с возможностью фильтрации и пагинации.
        Параметры фильтра передаются через query-параметры.
        """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Список комнат успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RoomListResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Ошибка в параметрах запроса"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<RoomListResponse> filterRooms(
            @ParameterObject
            @Valid RoomFilter filter,

            @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        RoomListResponse response = roomService.findAll(filter, pageable);

        return ResponseEntity.ok(response);
    }
}
