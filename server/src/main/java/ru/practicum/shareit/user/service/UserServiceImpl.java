package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
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
    public UserDto createUser(UserDto userDto) {
        log.debug("Создание нового пользователя: {}", userDto);
        User userToCreate = userMapper.mapToUser(userDto);

        User newUser = userRepository.save(userToCreate);
        log.debug("Пользователь с Id {} создан", newUser.getId());
        return userMapper.mapToUserDto(newUser);
    }

    @Override
    public List<UserDto> findAll() {
        log.debug("Получение всех пользователей");
        return userMapper.mapToUserDto(userRepository.findAll());
    }

    @Override
    public UserDto findUser(Long userId) {
        log.debug("Поиск пользователя с Id {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        return userMapper.mapToUserDto(user);
    }

    @Override
    @Transactional
    public UserDto updateUser(UserDto newUser, Long userId) {
        log.debug("Обновление пользователя с id = {}", userId);
        User oldUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

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
