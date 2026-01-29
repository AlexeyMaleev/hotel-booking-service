package com.example.hotels_app.controller;

import com.example.hotels_app.model.request.UpsertUserRequest;
import com.example.hotels_app.model.response.UserResponse;
import com.example.hotels_app.service.UserService;
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

import java.util.List;

@Tag(
        name = "Users",
        description = "Операции с пользователями"
)
@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Получить всех пользователей",
            description = "Возвращает список всех пользователей" +
                    " Доступно авторизованным пользователям с ролью USER или ADMIN"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Возвращен список всех пользователей"
            ),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers(){
        List<UserResponse> responseList = userService.findAll();
        return ResponseEntity.ok()
                .body(responseList);
    }

    @Operation(
            summary = "Получить пользователя по id",
            description = "Возвращает пользователя по указанному идентификатору." +
                    " Доступно авторизованным пользователям с ролью USER или ADMIN"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class)
                    )
            ),
            @ApiResponse( responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse( responseCode = "404", description = "Пользователь не найден")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserResponse> getById(
            @Parameter(description = "Id пользователя", example = "1")
            @PathVariable Long id){
        return ResponseEntity.ok().body(userService.findById(id));
    }

    @Operation(
            summary = "Найти пользователя по имени",
            description = "Возвращает пользователя по указанному имени." +
                    " Доступно авторизованным пользователям с ролью USER или ADMIN"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class)
                    )
            ),
            @ApiResponse( responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse( responseCode = "404", description = "Пользователь не найден")
    })
    @GetMapping("/by-name/{name}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserResponse> getByName(
            @Parameter(description = "Имя пользователя", example = "user")
            @PathVariable String name){
        return ResponseEntity.ok().body(userService.findByName(name));
    }

    @Operation(
            summary = "Создать пользователя",
            description = "Доступно всем"
    )
    @ApiResponses({
            @ApiResponse( responseCode = "201", description = "Пользователь создан"),
            @ApiResponse( responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse( responseCode = "409", description = "Пользователь с таким именем (или почтой) уже существует")
    })
    @PostMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<UserResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные нового пользователя",
                    required = true
            )
            @Valid @RequestBody UpsertUserRequest request){
        UserResponse response  = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Обновить данные пользователя",
            description = " Доступно авторизованным пользователям с ролью USER или ADMIN"
    )
    @ApiResponses({
            @ApiResponse( responseCode = "200", description = "Данные пользователя обновлены"),
            @ApiResponse( responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse( responseCode = "401", description = "Ошибка авторизации"),
            @ApiResponse( responseCode = "409", description = "Пользователь с таким именем (или почтой) уже существует")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserResponse> update(
            @Parameter(description = "Id пользователя", example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Новые данные пользователя",
                    required = true
            )
            @RequestBody UpsertUserRequest request){
        UserResponse response  = userService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Удалить пользователя по id",
            description = "Удаляет пользователя по указанному идентификатору." +
                    " Доступно авторизованным пользователям с ролью USER или ADMIN"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Пользователь удален"
            ),
            @ApiResponse( responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse( responseCode = "401", description = "Пользователь не авторизован")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        userService.deleteById(id);
        return  ResponseEntity.noContent().build();
    }
}
