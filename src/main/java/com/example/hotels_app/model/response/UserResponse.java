package com.example.hotels_app.model.response;

import com.example.hotels_app.security.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Информация о пользователе")
public class UserResponse {

    @Schema(description = "ID пользователя", example = "1")
    private Long id;

    @Schema(description = "Имя (логин) пользователя", example = "disruptor")
    private String name;

    @Schema(description = "Почтовый адрес", example = "disruptor@example.com")
    private String email;

    @Schema(
            description = "Роль пользователя в системе",
            example = "USER",
            allowableValues = {"USER", "ADMIN"}
    )
    private Role role;
}
