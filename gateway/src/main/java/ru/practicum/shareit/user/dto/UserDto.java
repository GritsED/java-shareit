package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    Long id;
    @NotBlank(message = "Имя не может быть пустым")
    String name;
    @NotBlank(message = "Емейл не может быть пустым")
    @Email(message = "Формат емейла qwerty@qwe.ru")
    String email;
}
