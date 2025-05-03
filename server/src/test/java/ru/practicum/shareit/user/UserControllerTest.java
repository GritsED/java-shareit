package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();
    @MockBean
    private UserService userService;
    @Autowired
    private MockMvc mvc;

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
    void findAll_shouldReturnUsersAndStatusIsOk() throws Exception {
        when(userService.findAll()).thenReturn(users);

        mvc.perform(get("/users"))
                .andExpectAll(status().isOk(),
                        jsonPath("$.length()").value(users.size()),
                        jsonPath("$[0].id").value(users.get(0).getId()),
                        jsonPath("$[0].name").value(users.get(0).getName()),
                        jsonPath("$[0].email").value(users.get(0).getEmail()));
    }

    @Test
    void findUser_shouldReturnUserById() throws Exception {
        when(userService.findUser(anyLong())).thenReturn(userDto);

        mvc.perform(get("/users/1"))
                .andExpectAll(status().isOk(),
                        jsonPath("$.id").value(1L),
                        jsonPath("$.name").value("Name"),
                        jsonPath("$.email").value("Email@email.ru")
                );
    }

    @Test
    void createUser() throws Exception {
        UserDto userToCreate = UserDto.builder()
                .name("Bob")
                .email("qwer@qwr.ru").build();

        UserDto createdUser = UserDto.builder()
                .id(1L)
                .name("Bob")
                .email("qwer@qwr.ru").build();

        when(userService.createUser(userToCreate)).thenReturn(createdUser);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsBytes(userToCreate))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$.id").value(1L),
                        jsonPath("$.name").value("Bob"),
                        jsonPath("$.email").value("qwer@qwr.ru"));
    }

    @Test
    void updateUser() throws Exception {
        UserDto updatedUser = UserDto.builder()
                .id(1L)
                .name("Update")
                .email("Update@email.ru").build();

        when(userService.updateUser(any(UserDto.class), anyLong()))
                .thenReturn(updatedUser);

        mvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsBytes(updatedUser))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$.id").value(1L),
                        jsonPath("$.name").value("Update"),
                        jsonPath("$.email").value("Update@email.ru"));
    }

    @Test
    void removeUser() throws Exception {
        doNothing().when(userService).removeUser(anyLong());

        mvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        verify(userService).removeUser(1L);
    }
}