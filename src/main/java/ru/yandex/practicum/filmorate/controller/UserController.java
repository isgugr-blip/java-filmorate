package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.utils.Utils;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    HashMap<Long, User> users = new HashMap<>();

    @PostMapping
    public User create(@Valid @RequestBody User body) {
        if (body.getEmail() == null || !Pattern.matches("^(.+)@(\\S+)$", body.getEmail())) {
            log.error("Invalid email {}", body.getEmail());
            throw new ConditionsNotMetException("Invalid email");
        }
        if (body.getLogin() == null || !Pattern.matches(".*\\S.*", body.getLogin())) {
            log.error("Invalid login {}", body.getLogin());
            throw new ConditionsNotMetException("Invalid login");
        }
        if (body.getBirthday() != null &&  body.getBirthday().toInstant().isAfter(Instant.now())) {
            log.error("Invalid birthday {}", body.getBirthday());
            throw new ConditionsNotMetException("Invalid birthday");
        }

        if (body.getEmail() != null) {
            findByEmail(body.getEmail()).ifPresent(u -> {
                log.error("Duplicate email {}", u.getEmail());
                throw new ConditionsNotMetException("Email already exists");
            });
        }

        User user = User.builder()
                .id(Utils.getNextId(users))
                .email(body.getEmail())
                .login(body.getLogin())
                .name(
                        body.getName() == null || body.getName().trim().isBlank() ?
                                body.getLogin() : body.getName().trim()
                )
                .birthday(body.getBirthday())
                .build();

        users.put(user.getId(), user);
        log.info("Created user {}", user);

        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User body) {
        if (body.getId() == null) {
            log.error("ID is null");
            throw new ConditionsNotMetException("Invalid id");
        }
        User user = users.get(body.getId());

        if (user == null) {
            log.error("User not found {}", body.getId());
            throw new NotFoundException("User not found");
        }

        if (!body.getEmail().equals(user.getEmail())) {
            findByEmail(body.getEmail()).ifPresent(u -> {
                if (!u.getId().equals(body.getId())) {
                    log.error("Duplicate email {}", u.getEmail());
                    throw new ConditionsNotMetException("Email already exists");
                }
            });
        }

        log.info("Current user {}", user);

        User updatedUser = user.toBuilder()
                .email(body.getEmail())
                .login(body.getLogin())
                .name(body.getName())
                .birthday(body.getBirthday())
                .build();

        users.put(updatedUser.getId(), updatedUser);
        log.info("Updated user {}", updatedUser);

        return updatedUser;
    }

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    public Optional<User> findByEmail(String email) {
        return users.values()
                .stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst();
    }
}
