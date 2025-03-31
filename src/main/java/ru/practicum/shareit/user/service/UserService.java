package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    UserDto createUser(UserDto userDto);

    List<UserDto> findAll();

    User findUser(Long userId);

    UserDto updateUser(User user, Long id);

    void removeUser(Long userId);
}
