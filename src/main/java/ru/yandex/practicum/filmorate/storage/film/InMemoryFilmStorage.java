package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.utils.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final HashMap<Long, Film> films = new HashMap<>();

    @Override
    public Film create(Film film) {
        log.info("Creating film {}", film);
        Film newFilm = Film.builder()
                .id(Utils.getNextId(films))
                .releaseDate(film.getReleaseDate())
                .name(film.getName())
                .description(film.getDescription())
                .duration(film.getDuration())
                .build();
        films.put(newFilm.getId(), newFilm);
        return newFilm;
    }

    @Override
    public Film update(Long id, Film updatedFilm) {
        if (updatedFilm.getId() == null) {
            log.error("ID is null");
            throw new ConditionsNotMetException("Invalid id", "id", updatedFilm.getId());
        }
        Film film = films.get(updatedFilm.getId());

        if (film == null) {
            log.error("Film with id {} not found", updatedFilm.getId());
            throw new NotFoundException("Film not found");
        }

        log.info("Current film {}", film);

        Film newFilm = film.toBuilder()
                .releaseDate(updatedFilm.getReleaseDate())
                .name(updatedFilm.getName())
                .description(updatedFilm.getDescription())
                .duration(updatedFilm.getDuration())
                .build();

        films.put(id, newFilm);
        log.info("Updated film {}", newFilm);
        return newFilm;
    }

    @Override
    public boolean existsById(Long id) {
        return films.containsKey(id);
    }

    @Override
    public Optional<Film> getById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Collection<Film> getAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public void delete(Long id) {
        films.remove(id);
    }
}
