package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringJUnitConfig
@Import(UserServiceImpl.class)
class UserServiceImplTest {

    @MockBean
    private UserRepository userRepository;
    @MockBean
    private UserMapper userMapper;
    @Autowired
    private UserService userService;

    private UserDto userDto;
    private UserDto userDto2;
    private List<UserDto> users;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .id(1L)
                .name("Name")
                .email("Email@email.ru")
                .build();
        userDto2 = UserDto.builder()
                .id(2L)
                .name("Name2")
                .email("Email2@email2.ru")
                .build();

        users = List.of(userDto, userDto2);
    }

    @Test
    void createUser_shouldReturnUser() {
        User userToCreate = new User();
        userToCreate.setId(1L);
        userToCreate.setName(userDto.getName());
        userToCreate.setEmail(userDto.getEmail());

        when(userMapper.mapToUser(userDto)).thenReturn(userToCreate);

        when(userRepository.save(userToCreate)).thenReturn(userToCreate);

        when(userMapper.mapToUserDto(userToCreate)).thenReturn(userDto);

        UserDto createdUser = userService.createUser(userDto);

        assertNotNull(createdUser);
        assertEquals(userDto.getName(), createdUser.getName());
        assertEquals(userDto.getEmail(), createdUser.getEmail());

        verify(userRepository, times(1)).save(userToCreate);
        verify(userMapper, times(1)).mapToUser(userDto);
        verify(userMapper, times(1)).mapToUserDto(userToCreate);
    }

    @Test
    void findAll_shouldReturnUsers() {
        List<User> usersList = List.of(User.builder()
                        .id(1L)
                        .name("Name")
                        .email("Email@email.ru")
                        .build(),
                User.builder()
                        .id(2L)
                        .name("Name2")
                        .email("Email2@email2.ru")
                        .build());

        when(userRepository.findAll()).thenReturn(usersList);
        when(userMapper.mapToUserDto(usersList)).thenReturn(users);

        List<UserDto> result = userService.findAll();

        assertEquals(2, result.size());
        assertEquals(usersList.get(0).getName(), result.get(0).getName());
        assertEquals(usersList.get(0).getEmail(), result.get(0).getEmail());

        verify(userRepository, times(1)).findAll();
        verify(userMapper, times(1)).mapToUserDto(usersList);
    }

    @Test
    void findUser_shouldReturnUser() {
        User user = User.builder()
                .id(1L)
                .name("Name")
                .email("Email@email.ru")
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userMapper.mapToUserDto(user)).thenReturn(userDto);

        UserDto foundUser = userService.findUser(1L);

        assertNotNull(foundUser);
        assertEquals(userDto.getName(), foundUser.getName());
        assertEquals(userDto.getEmail(), foundUser.getEmail());

        verify(userRepository, times(1)).findById(1L);
        verify(userMapper, times(1)).mapToUserDto(user);
    }

    @Test
    void findUser_shouldReturnNotFoundException() {
        Long id = 999L;

        when(userRepository.findById(anyLong()))
                .thenThrow(new NotFoundException("Пользователь с id " + id + " не найден"));

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден")));
        assertEquals("Пользователь с id " + 999 + " не найден", exception.getMessage());

        verify(userRepository, times(1)).findById(id);
    }

    @Test
    void updateUser() {
        UserDto newUser = UserDto.builder()
                .id(1L)
                .name("Update")
                .email("Update@email.ru")
                .build();

        User oldUser = User.builder()
                .id(1L)
                .name("Name")
                .email("Email@email.ru")
                .build();

        User user = User.builder()
                .id(1L)
                .name("Update")
                .email("Update@email.ru")
                .build();


        when(userRepository.findById(anyLong())).thenReturn(Optional.of(oldUser));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.mapToUserDto(user)).thenReturn(newUser);

        UserDto updatedUser = userService.updateUser(newUser, oldUser.getId());

        assertEquals(newUser.getName(), updatedUser.getName());
        assertEquals(newUser.getEmail(), updatedUser.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_shouldNotUpdateName_NameIsNull() {
        UserDto newUser = UserDto.builder()
                .id(1L)
                .email("Update@email.ru")
                .build();

        User oldUser = User.builder()
                .id(1L)
                .name("Name")
                .email("Email@email.ru")
                .build();

        User updateUser = User.builder()
                .id(1L)
                .name("Name")
                .email("Update@email.ru")
                .build();


        when(userRepository.findById(anyLong())).thenReturn(Optional.of(oldUser));
        when(userRepository.save(any(User.class))).thenReturn(updateUser);
        when(userMapper.mapToUserDto(updateUser)).thenReturn(newUser);

        UserDto updatedUser = userService.updateUser(newUser, oldUser.getId());

        assertEquals(newUser.getName(), updatedUser.getName());
        assertEquals(newUser.getEmail(), updatedUser.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_shouldNotUpdateName_NameIsBlank() {
        UserDto newUser = UserDto.builder()
                .id(1L)
                .name("   ")
                .email("Update@email.ru")
                .build();

        User oldUser = User.builder()
                .id(1L)
                .name("Name")
                .email("Email@email.ru")
                .build();

        User updateUser = User.builder()
                .id(1L)
                .name("Name")
                .email("Update@email.ru")
                .build();


        when(userRepository.findById(anyLong())).thenReturn(Optional.of(oldUser));
        when(userRepository.save(any(User.class))).thenReturn(updateUser);
        when(userMapper.mapToUserDto(updateUser)).thenReturn(newUser);

        UserDto updatedUser = userService.updateUser(newUser, oldUser.getId());

        assertEquals(newUser.getName(), updatedUser.getName());
        assertEquals(newUser.getEmail(), updatedUser.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_shouldNotUpdateEmail_EmailIsNull() {
        UserDto newUser = UserDto.builder()
                .id(1L)
                .name("Update")
                .build();

        User oldUser = User.builder()
                .id(1L)
                .name("Name")
                .email("Email@email.ru")
                .build();

        User updateUser = User.builder()
                .id(1L)
                .name("Update")
                .email("Email@email.ru")
                .build();


        when(userRepository.findById(anyLong())).thenReturn(Optional.of(oldUser));
        when(userRepository.save(any(User.class))).thenReturn(updateUser);
        when(userMapper.mapToUserDto(updateUser)).thenReturn(newUser);

        UserDto updatedUser = userService.updateUser(newUser, oldUser.getId());

        assertEquals(newUser.getName(), updatedUser.getName());
        assertEquals(newUser.getEmail(), updatedUser.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_shouldNotUpdateEmail_EmailIsBlank() {
        UserDto newUser = UserDto.builder()
                .id(1L)
                .name("Update")
                .email("   ")
                .build();

        User oldUser = User.builder()
                .id(1L)
                .name("Name")
                .email("Email@email.ru")
                .build();

        User updateUser = User.builder()
                .id(1L)
                .name("Update")
                .email("Email@email.ru")
                .build();


        when(userRepository.findById(anyLong())).thenReturn(Optional.of(oldUser));
        when(userRepository.save(any(User.class))).thenReturn(updateUser);
        when(userMapper.mapToUserDto(updateUser)).thenReturn(newUser);

        UserDto updatedUser = userService.updateUser(newUser, oldUser.getId());

        assertEquals(newUser.getName(), updatedUser.getName());
        assertEquals(newUser.getEmail(), updatedUser.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_shouldReturnNoFoundException() {
        Long id = 999L;

        when(userRepository.findById(anyLong()))
                .thenThrow(new NotFoundException("Пользователь с id " + id + " не найден"));

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.updateUser(userDto, id));
        assertEquals("Пользователь с id " + 999 + " не найден", exception.getMessage());

        verify(userRepository, times(1)).findById(id);
    }

    @Test
    void removeUser() {
        Long id = 1L;
        doNothing().when(userRepository).deleteById(id);

        userService.removeUser(id);

        verify(userRepository, times(1)).deleteById(id);
    }
}