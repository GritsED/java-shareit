package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserRepository {
    User createUser(User user);

    User findUser(Long userId);

    User updateUser(User user, Long id);

    void removeUser(Long userId);

    List<UserDto> findAll();
}
