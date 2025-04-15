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


//    public static User mapToUser(UserDto userDto) {
//        return User.builder()
//                .name(userDto.getName())
//                .email(userDto.getEmail())
//                .build();
//    }
//
//    public static UserDto mapToUserDto(User user) {
//        return UserDto.builder()
//                .id(user.getId())
//                .name(user.getName())
//                .email(user.getEmail())
//                .build();
//    }
//
//    public static List<UserDto> mapToUserDto(Iterable<User> users) {
//        List<UserDto> result = new ArrayList<>();
//
//        for (User user : users) {
//            result.add(mapToUserDto(user));
//        }
//
//        return result;
//    }
}
