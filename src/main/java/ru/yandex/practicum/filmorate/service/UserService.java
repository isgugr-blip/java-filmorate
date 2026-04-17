package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final JdbcTemplate jdbcTemplate;

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
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("User with id " + userId + " does not exist");
        }
        if (!userStorage.existsById(friendId)) {
            throw new NotFoundException("User with id " + friendId + " does not exist");
        }

        // Односторонняя дружба: добавляем только запись (userId -> friendId)
        jdbcTemplate.update(
                "MERGE INTO friendships (user_id, friend_id, status) VALUES (?, ?, 'UNCONFIRMED')",
                userId, friendId
        );

        // Если обратная запись существует — обе становятся CONFIRMED
        Integer reverseCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ?",
                Integer.class, friendId, userId
        );
        if (reverseCount != null && reverseCount > 0) {
            jdbcTemplate.update(
                    "UPDATE friendships SET status = 'CONFIRMED' " +
                            "WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)",
                    userId, friendId, friendId, userId
            );
        }
    }

    public Collection<User> getFriends(Long userId) {
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("User with id " + userId + " does not exist");
        }

        List<Long> friendIds = jdbcTemplate.query(
                "SELECT friend_id FROM friendships WHERE user_id = ?",
                (rs, rowNum) -> rs.getLong("friend_id"),
                userId
        );

        return friendIds.stream()
                .map(userStorage::getById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("User with id " + userId + " does not exist");
        }
        if (!userStorage.existsById(otherId)) {
            throw new NotFoundException("User with id " + otherId + " does not exist");
        }

        List<Long> commonIds = jdbcTemplate.query(
                "SELECT f1.friend_id FROM friendships f1 " +
                        "JOIN friendships f2 ON f1.friend_id = f2.friend_id " +
                        "WHERE f1.user_id = ? AND f2.user_id = ?",
                (rs, rowNum) -> rs.getLong("friend_id"),
                userId, otherId
        );

        return commonIds.stream()
                .map(userStorage::getById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    public void removeFriend(Long userId, Long friendId) {
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("User with id " + userId + " does not exist");
        }
        if (!userStorage.existsById(friendId)) {
            throw new NotFoundException("User with id " + friendId + " does not exist");
        }

        jdbcTemplate.update(
                "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?",
                userId, friendId
        );

        // Если обратная запись была CONFIRMED — понизить до UNCONFIRMED
        jdbcTemplate.update(
                "UPDATE friendships SET status = 'UNCONFIRMED' " +
                        "WHERE user_id = ? AND friend_id = ?",
                friendId, userId
        );
    }
}
