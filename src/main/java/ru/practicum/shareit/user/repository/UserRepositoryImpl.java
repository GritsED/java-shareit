package ru.practicum.shareit.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
public class UserRepositoryImpl implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User createUser(User user) {
        log.debug("Создание нового пользователя: {}", user);
        emailExist(user);
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.debug("Пользователь с Id {} создан", user.getId());
        return user;
    }

    @Override
    public User findUser(Long userId) {
        log.debug("Поиск пользователя с Id {}", userId);
        return Optional.ofNullable(users.get(userId))
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    @Override
    public List<UserDto> findAll() {
        log.debug("Получение всех пользователей");
        return users.values().stream()
                .map(UserMapper::MapToUserDto)
                .toList();
    }

    @Override
    public User updateUser(User newUser, Long userId) {
        log.debug("Обновление пользователя с id = {}", userId);
        if (users.containsKey(userId)) {
            User oldUSer = users.get(userId);
            if (newUser.getName() != null) {
                oldUSer.setName(newUser.getName());
            }
            if (newUser.getEmail() != null) {
                emailExist(newUser);
                oldUSer.setEmail(newUser.getEmail());
            }
            return oldUSer;
        } else {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
    }

    @Override
    public void removeUser(Long userId) {
        log.debug("Удаление пользователя с id = {}", userId);
        users.remove(userId);
    }

    private void emailExist(User user) {
        users.values()
                .forEach(u -> {
                    if (u.getEmail().equals(user.getEmail()) && !u.getId().equals(user.getId())) {
                        log.error("Данный email = {} уже занят", user.getEmail());
                        throw new ConflictException("Данный email занят");
                    }
                });
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
