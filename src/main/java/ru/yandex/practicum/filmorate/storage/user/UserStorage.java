package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    User create(User user);

    User update(Long id, User user);

    boolean existsById(Long id);

    boolean emailExists(String email);

    Optional<User> getById(Long id);

    Optional<User> getByEmail(String email);

    Collection<User> getAllUsers();

    void delete(Long id);
}
