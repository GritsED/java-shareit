package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository ur;

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = UserMapper.MapToUser(userDto);
        User user1 = ur.createUser(user);
        return UserMapper.MapToUserDto(user1);
    }

    @Override
    public List<UserDto> findAll() {
        return ur.findAll();
    }

    @Override
    public User findUser(Long userId) {
        return ur.findUser(userId);
    }

    @Override
    public UserDto updateUser(User newUser, Long userId) {
        User user = ur.updateUser(newUser, userId);
        return UserMapper.MapToUserDto(user);
    }

    @Override
    public void removeUser(Long userId) {
        ur.removeUser(userId);
    }
}
