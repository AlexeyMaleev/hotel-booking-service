package com.example.hotels_app.model.request;

import com.example.hotels_app.security.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Данные для создания/обновления пользователя")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpsertUserRequest {

    @Schema(description = "Имя (логин) пользователя ", example = "user")
    @NotBlank
    private String name;

    @Schema(description = "Пароль пользователя ", example = "pasSword#8", format = "password" )
    @NotBlank
    private String password;

    @Schema(description = "Почтовый ящик (email) пользователя ", example = "user@mail.ru")
    @NotBlank
    @Email(message = "Некорректный формат email")
    private String email;

    @Schema(
            description = "Роль пользователя в системе. " +
                    "Определяет уровень доступа к функционалу приложения. " +
                    "Возможные значения: USER, ADMIN",
            example = "USER"
    )
    @NotNull
    private Role role;
}
