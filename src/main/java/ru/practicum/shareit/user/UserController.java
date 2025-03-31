package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {
    private final UserService us;

    @GetMapping
    public List<UserDto> findAll() {
        return us.findAll();
    }

    @GetMapping("/{userId}")
    public User findUser(@PathVariable Long userId) {
        return us.findUser(userId);
    }

    @PostMapping
    public UserDto create(@RequestBody @Valid UserDto userDto) {
        return us.createUser(userDto);
    }

    @PatchMapping("/{userId}")
    public UserDto update(@RequestBody User user, @PathVariable Long userId) {
        return us.updateUser(user, userId);
    }

    @DeleteMapping("/{userId}")
    public void remove(@PathVariable Long userId) {
        us.removeUser(userId);
    }
}
