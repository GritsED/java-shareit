package ru.practicum.shareit.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    User mapToUser(UserDto userDto);

    UserDto mapToUserDto(User user);

    List<UserDto> mapToUserDto(Iterable<User> users);
}
