package ru.practicum.shareit.user.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    private final UserMapperImpl mapper = new UserMapperImpl();

    @Test
    void mapToUser_nullReturnsNull() {
        assertNull(mapper.mapToUser(null));
    }

    @Test
    void mapToUserDto_nullReturnsNull() {
        assertNull(mapper.mapToUserDto((User) null));
    }

    @Test
    void mapToUserDtoIterable_nullReturnsNull() {
        assertNull(mapper.mapToUserDto((Iterable<User>) null));
    }

    @Test
    void mapToUser_andBack() {
        UserDto dto = UserDto.builder()
                .id(1L)
                .name("Alice")
                .email("alice@mail.com")
                .build();

        User user = mapper.mapToUser(dto);
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("Alice", user.getName());

        UserDto dto2 = mapper.mapToUserDto(user);
        assertNotNull(dto2);
        assertEquals(dto.getEmail(), dto2.getEmail());
    }

    @Test
    void mapIterable() {
        User u = User.builder().id(2L).name("Bob").email("b@mail").build();
        List<UserDto> dtos = mapper.mapToUserDto(List.of(u));
        assertEquals(1, dtos.size());
        assertEquals(u.getId(), dtos.get(0).getId());
    }
}
