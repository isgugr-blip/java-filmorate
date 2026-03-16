package ru.yandex.practicum.filmorate.model;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder(toBuilder = true)
public class User {
    private Long id;

    @Email
    private String email;

    @NotNull
    @NotBlank
    private String login;

    @Nullable
    private String name;

    @Past
    private LocalDate birthday;

    @Builder.Default
    private Set<Long> friends = new HashSet<>();
}
