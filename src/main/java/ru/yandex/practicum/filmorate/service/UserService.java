package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.UserCreateDto;
import ru.yandex.practicum.filmorate.dto.UserResponseDto;
import ru.yandex.practicum.filmorate.dto.UserUpdateDto;
import ru.yandex.practicum.filmorate.dto.mapper.UserMapper;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public UserResponseDto createUser(UserCreateDto dto) {
        User user = UserMapper.toUser(dto);
        return UserMapper.toResponse(userStorage.create(user));
    }

    public UserResponseDto updateUser(UserUpdateDto dto) {
        if (dto.getId() == null) {
            throw new NotFoundException("User id must not be null");
        }
        if (!userStorage.existsById(dto.getId())) {
            throw new NotFoundException("User with id " + dto.getId() + " does not exist");
        }
        User user = UserMapper.toUser(dto);
        return UserMapper.toResponse(userStorage.update(dto.getId(), user));
    }

    public UserResponseDto getUserById(Long id) {
        User user = userStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));
        return UserMapper.toResponse(user);
    }

    public Collection<UserResponseDto> getAllUsers() {
        return userStorage.getAllUsers().stream()
                .map(UserMapper::toResponse)
                .collect(Collectors.toList());
    }

    public void addFriend(Long userId, Long friendId) {
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("User with id " + userId + " does not exist");
        }
        if (!userStorage.existsById(friendId)) {
            throw new NotFoundException("User with id " + friendId + " does not exist");
        }
        userStorage.addFriend(userId, friendId);
    }

    public Collection<UserResponseDto> getFriends(Long userId) {
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("User with id " + userId + " does not exist");
        }
        return userStorage.getFriends(userId).stream()
                .map(UserMapper::toResponse)
                .collect(Collectors.toList());
    }

    public Collection<UserResponseDto> getCommonFriends(Long userId, Long otherId) {
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("User with id " + userId + " does not exist");
        }
        if (!userStorage.existsById(otherId)) {
            throw new NotFoundException("User with id " + otherId + " does not exist");
        }
        return userStorage.getCommonFriends(userId, otherId).stream()
                .map(UserMapper::toResponse)
                .collect(Collectors.toList());
    }

    public void removeFriend(Long userId, Long friendId) {
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("User with id " + userId + " does not exist");
        }
        if (!userStorage.existsById(friendId)) {
            throw new NotFoundException("User with id " + friendId + " does not exist");
        }
        userStorage.removeFriend(userId, friendId);
    }
}
