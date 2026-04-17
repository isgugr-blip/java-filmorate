package ru.yandex.practicum.filmorate.exceptions;

import lombok.Getter;

public class ConditionsNotMetException extends RuntimeException {
    @Getter
    private final String field;
    @Getter
    private final Object value;

    public ConditionsNotMetException(String message, String field, Object value) {
        super(message);
        this.field = field;
        this.value = value;
    }

}
