package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.utils.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final HashMap<Long, User> users = new HashMap<>();

    @Override
    public User create(User user) {
        log.info("Creating user {}", user);
        User newUser = User.builder()
                .id(Utils.getNextId(users))
                .email(user.getEmail())
                .login(user.getLogin())
                .name(
                        user.getName() == null || user.getName().trim().isBlank() ?
                                user.getLogin() : user.getName().trim()
                )
                .birthday(user.getBirthday())
                .build();

        users.put(newUser.getId(), newUser);
        return newUser;
    }

    @Override
    public User update(Long id, User updatedUser) {
        users.replace(id, updatedUser);
        return updatedUser;
    }

    @Override
    public Optional<User> getById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Optional<User> getByEmail(String email) {
        return users.values()
                .stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public Collection<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void delete(Long id) {
        users.remove(id);
    }
}
