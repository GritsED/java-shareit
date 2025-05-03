package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto createUser(UserDto user);

    List<UserDto> findAll();

    UserDto findUser(Long userId);

    UserDto updateUser(UserDto user, Long id);

    void removeUser(Long userId);
}
