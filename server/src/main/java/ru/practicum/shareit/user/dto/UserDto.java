package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    Long id;
    @NotBlank(message = "Имя не может быть пустым")
    String name;
    @NotBlank(message = "Емейл не может быть пустым")
    @Email(message = "Формат емейла qwerty@qwe.ru")
    String email;
}
