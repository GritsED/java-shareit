package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto createUser(User user) {
        log.debug("Создание нового пользователя: {}", user);
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ValidationException("Почта должна быть указана");
        }
        User newUser = userRepository.save(user);
        log.debug("Пользователь с Id {} создан", newUser.getId());
        return userMapper.mapToUserDto(newUser);
    }

    @Override
    public List<UserDto> findAll() {
        log.debug("Получение всех пользователей");
        return userMapper.mapToUserDto(userRepository.findAll());
    }

    @Override
    public User findUser(Long userId) {
        log.debug("Поиск пользователя с Id {}", userId);
        return userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }

    @Override
    @Transactional
    public UserDto updateUser(User newUser, Long userId) {
        log.debug("Обновление пользователя с id = {}", userId);
        if (userId == null) {
            log.warn("Ошибка обновления пользователя: Id не указан");
            throw new IllegalArgumentException("Id должен быть указан.");
        }
        User oldUser = findUser(userId);

        if (newUser.getName() != null && !newUser.getName().isBlank()) {
            oldUser.setName(newUser.getName());
        }

        if (newUser.getEmail() != null && !newUser.getEmail().isBlank()) {
            oldUser.setEmail(newUser.getEmail());
        }

        User user = userRepository.save(oldUser);
        return userMapper.mapToUserDto(user);
    }

    @Override
    @Transactional
    public void removeUser(Long userId) {
        log.debug("Удаление пользователя с id = {}", userId);
        userRepository.deleteById(userId);
    }
}
