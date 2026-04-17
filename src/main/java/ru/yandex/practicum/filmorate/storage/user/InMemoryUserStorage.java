package ru.yandex.practicum.filmorate.storage.user;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.utils.Utils;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final HashMap<Long, User> users = new HashMap<>();
    private final HashSet<String> existingEmails = new HashSet<>();

    @PostConstruct
    public void initEmails() {
        users.values().forEach(user -> existingEmails.add(user.getEmail()));
    }

    @Override
    public boolean emailExists(String email) {
        return existingEmails.contains(email);
    }

    @Override
    public User create(User user) {
        if (existingEmails.contains(user.getEmail())) {
            log.error("Duplicate email {}", user.getEmail());
            throw new ConditionsNotMetException("Email already exists", "email", user.getEmail());
        }

        existingEmails.add(user.getEmail());

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
        User existingUser = users.get(id);
        User newUser = existingUser.toBuilder()
                .email(updatedUser.getEmail())
                .login(updatedUser.getLogin())
                .name(
                        updatedUser.getName() == null || updatedUser.getName().trim().isBlank() ?
                                updatedUser.getLogin() : updatedUser.getName().trim()
                )
                .birthday(updatedUser.getBirthday())
                .build();
        users.replace(id, newUser);
        return newUser;
    }

    @Override
    public boolean existsById(Long id) {
        return users.containsKey(id);
    }

    @Override
    public Optional<User> getById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Optional<User> getByEmail(String email) {
        return users.values()
                .stream()
                .filter(u -> Objects.equals(u.getEmail(), email))
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
