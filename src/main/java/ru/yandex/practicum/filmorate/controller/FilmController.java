package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.utils.Utils;

import java.util.Collection;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    HashMap<Long, Film> films = new HashMap<>();
    @PostMapping
    public Film create(@Valid @RequestBody Film body) {
        Film film = Film.builder()
                .id(Utils.getNextId(films))
                .releaseDate(body.getReleaseDate())
                .name(body.getName())
                .description(body.getDescription())
                .duration(body.getDuration())
                .build();
        films.put(film.getId(), film);
        log.info("Creating film {}", film);

        return film;
    }
    @PutMapping
    public Film update(@Valid @RequestBody(required = false) Film body) {
        if (body == null) {
            return null;
        }
        if (body.getId() == null) {
            log.error("ID is null");
            throw new ConditionsNotMetException("Invalid id");
        }
        Film film = films.get(body.getId());

        if (film == null) {
            log.error("Film with id {} not found", body.getId());
            throw new NotFoundException("Film not found");
        }

        log.info("Current film {}", film);

        Film updatedFilm = film.toBuilder()
                .releaseDate(body.getReleaseDate())
                .name(body.getName())
                .description(body.getDescription())
                .duration(body.getDuration())
                .build();

        films.put(updatedFilm.getId(), updatedFilm);
        log.info("Updated film {}", updatedFilm);

        return updatedFilm;
    }
    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }
}
