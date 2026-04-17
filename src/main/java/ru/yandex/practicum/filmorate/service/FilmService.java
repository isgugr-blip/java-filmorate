package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;
    private final JdbcTemplate jdbcTemplate;

    public Film addNewFilm(Film film) {
        validateMpaAndGenres(film);
        return filmStorage.create(film);
    }

    public Film updateFilm(Long id, Film film) {
        if (!filmStorage.existsById(id)) {
            throw new NotFoundException("Film with id " + id + " does not exist");
        }
        validateMpaAndGenres(film);
        return filmStorage.update(id, film);
    }

    private void validateMpaAndGenres(Film film) {
        if (film.getMpa() != null) {
            mpaStorage.getById(film.getMpa().getId())
                    .orElseThrow(() -> new NotFoundException("MPA rating with id " + film.getMpa().getId() + " not found"));
        }
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                genreStorage.getById(genre.getId())
                        .orElseThrow(() -> new NotFoundException("Genre with id " + genre.getId() + " not found"));
            }
        }
    }

    public Optional<Film> getFilmById(Long id) {
        return filmStorage.getById(id);
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getAll();
    }

    public void addLike(Long filmId, Long userId) {
        if (!filmStorage.existsById(filmId)) {
            throw new NotFoundException("Film with id " + filmId + " does not exist");
        }
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("User with id " + userId + " does not exist");
        }
        jdbcTemplate.update(
                "MERGE INTO film_likes (film_id, user_id) VALUES (?, ?)",
                filmId, userId
        );
    }

    public void removeLike(Long filmId, Long userId) {
        if (!filmStorage.existsById(filmId)) {
            throw new NotFoundException("Film with id " + filmId + " does not exist");
        }
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("User with id " + userId + " does not exist");
        }
        jdbcTemplate.update(
                "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?",
                filmId, userId
        );
    }

    public Collection<Film> getFilmsByLikes(int count) {
        List<Long> filmIds = jdbcTemplate.query(
                "SELECT f.id FROM films f " +
                        "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                        "GROUP BY f.id " +
                        "ORDER BY COUNT(fl.user_id) DESC " +
                        "LIMIT ?",
                (rs, rowNum) -> rs.getLong("id"),
                count
        );

        return filmIds.stream()
                .map(filmStorage::getById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }
}
