package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User createUser(User user) {
        return userStorage.create(user);
    }

    public User updateUser(User updatedUser) {
        if (updatedUser.getId() == null) {
            log.error("ID is null");
            throw new ConditionsNotMetException("Invalid ID", "id", updatedUser.getId());
        }

        var optionalUser = userStorage.getById(updatedUser.getId());

        optionalUser.ifPresentOrElse(
                user -> {
                    if (!updatedUser.getEmail().equals(user.getEmail()) && userStorage.emailExists(updatedUser.getEmail())) {
                        log.error("Duplicate email {}", user.getEmail());
                        throw new ConditionsNotMetException("Email already exists", "email", updatedUser.getEmail());
                    }
                },
                () -> {
                    log.error("User with id {} does not exist", updatedUser.getId());
                    throw new NotFoundException("User with id " + updatedUser.getId() + " does not exist");
                }
        );

        return userStorage.update(updatedUser.getId(), updatedUser);
    }

    public Optional<User> getUserById(Long id) {
        return userStorage.getById(id);
    }

    public Collection<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public void addFriend(Long userId, Long friendId) {
        User user1 = userStorage.getById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " does not exist"));
        User user2 = userStorage.getById(friendId)
                .orElseThrow(() -> new NotFoundException("User with id " + friendId + " does not exist"));

        user1.getFriends().add(friendId);
        user2.getFriends().add(userId);
    }

    public Collection<User> getFriends(Long userId) {
        User user = userStorage.getById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " does not exist"));

        return user.getFriends().stream()
                .map(userStorage::getById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        User user1 = userStorage.getById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " does not exist"));
        User user2 = userStorage.getById(otherId)
                .orElseThrow(() -> new NotFoundException("User with id " + otherId + " does not exist"));

        Set<Long> commonFriendIds = new HashSet<>(user1.getFriends());
        commonFriendIds.retainAll(user2.getFriends());
        return commonFriendIds.stream()
                .map(userStorage::getById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    public void removeFriend(Long userId, Long friendId) {
        User user1 = userStorage.getById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " does not exist"));
        User user2 = userStorage.getById(friendId)
                .orElseThrow(() -> new NotFoundException("User with id " + friendId + " does not exist"));

        user1.getFriends().remove(friendId);
        user2.getFriends().remove(userId);
    }
}
