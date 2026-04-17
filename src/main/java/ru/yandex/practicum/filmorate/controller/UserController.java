package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.UserCreateDto;
import ru.yandex.practicum.filmorate.dto.UserResponseDto;
import ru.yandex.practicum.filmorate.dto.UserUpdateDto;
import ru.yandex.practicum.filmorate.dto.mapper.UserMapper;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public UserResponseDto create(@Valid @RequestBody UserCreateDto body) {
        return UserMapper.toResponse(userService.createUser(UserMapper.toUser(body)));
    }

    @PutMapping
    public UserResponseDto update(@Valid @RequestBody UserUpdateDto body) {
        return UserMapper.toResponse(userService.updateUser(UserMapper.toUser(body)));
    }

    @GetMapping
    public Collection<UserResponseDto> findAll() {
        return userService.getAllUsers().stream()
                .map(UserMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public UserResponseDto findById(@PathVariable Long id) {
        return UserMapper.toResponse(
                userService.getUserById(id)
                        .orElseThrow(() -> new NotFoundException("User with id " + id + " not found"))
        );
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        userService.addFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public Collection<UserResponseDto> findFriends(@PathVariable Long id) {
        return userService.getFriends(id).stream()
                .map(UserMapper::toResponse)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable Long id, @PathVariable Long friendId) {
        userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<UserResponseDto> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        return userService.getCommonFriends(id, otherId).stream()
                .map(UserMapper::toResponse)
                .collect(Collectors.toList());
    }
}
