package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    public User create(User user);

    public User update(Long id, User user);

    public Optional<User> getById(Long id);

    public Optional<User> getByEmail(String email);

    public Collection<User> getAllUsers();

    public void delete(Long id);
}
