package com.example.hotels_app.controller;

import com.example.hotels_app.model.request.CreateBookingRequest;
import com.example.hotels_app.model.request.UpdateBookingRequest;
import com.example.hotels_app.model.response.BookingListResponse;
import com.example.hotels_app.model.response.BookingResponse;
import com.example.hotels_app.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@Tag(
        name = "Bookings",
        description = "Операции с бронированиями"
)
@RestController
@RequestMapping("/api/v1/bookings")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @Operation(
            summary = "Создать бронирование",
            description = "Создает новое бронирование." +
                    " Доступно авторизованным пользователям с ролью USER или ADMIN"
    )
    @ApiResponses({
            @ApiResponse( responseCode = "201", description = "Бронирование создана"),
            @ApiResponse( responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse( responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "409", description = "Комната уже забронирована на указанные даты")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<BookingResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для создания бронирования",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateBookingRequest.class)
                    )
            )
            @Valid @RequestBody CreateBookingRequest request){
        BookingResponse response = bookingService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Изменить бронирование",
            description = "Изменить данные бронирования." +
                    " Доступно авторизованным пользователям с ролью USER или ADMIN"
    )
    @ApiResponses({
            @ApiResponse( responseCode = "200", description = "Данные бронирования обновлены"),
            @ApiResponse( responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse( responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse( responseCode = "409", description = "Комната уже забронирована на указанные даты")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<BookingResponse> update(
            @Parameter(description = "ID бронирования", example = "1")
            @PathVariable Long id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для обновления бронирования",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UpdateBookingRequest.class)
                    )
            )
            @Valid @RequestBody UpdateBookingRequest request){

        return ResponseEntity.ok()
                .body(bookingService.update(id, request));
    }

    @Operation(
            summary = "Получить бронирование по id",
            description = "Возвращает бронирование по указанному идентификатору." +
                    " Доступно авторизованным пользователям с ролью USER или ADMIN"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Бронирование найдено",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BookingResponse.class)
                    )
            ),
            @ApiResponse( responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse( responseCode = "404", description = "Бронирование не найдено")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<BookingResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok().body(bookingService.findById(id));
    }


    @Operation(
            summary = "Получить список всех бронирований",
            description = "Возвращает список бронирований с возможностью пагинации."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Список бронирований успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BookingListResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Ошибка в параметрах запроса"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BookingListResponse> getAllBookings(
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы", example = "10")
            @RequestParam(defaultValue = "10") int size){
        BookingListResponse responseList = bookingService.getAll(page, size);

        return ResponseEntity.ok()
                .body(responseList);
    }


    @Operation(
            summary = "Удалить бронирование по id",
            description = "Удаляет бронирование по указанному идентификатору." +
                    " Доступно авторизованным пользователям с ролью USER или ADMIN"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Бронирование удалено"
            ),
            @ApiResponse( responseCode = "404", description = "Бронирование не найдено"),
            @ApiResponse( responseCode = "401", description = "Пользователь не авторизован")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        bookingService.deleteById(id);
        return  ResponseEntity.noContent().build();
    }
}
