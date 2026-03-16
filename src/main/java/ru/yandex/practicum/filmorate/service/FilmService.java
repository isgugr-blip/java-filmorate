package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film addNewFilm(Film film) {
        return filmStorage.create(film);
    }

    public Film updateFilm(Long id, Film film) {
        return filmStorage.update(id, film);
    }

    public Optional<Film> getFilmById(Long id) {
        return filmStorage.getById(id);
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getAll();
    }

    public void addLike(Long id, Long userId) {
        Film film = getFilmById(id)
                .orElseThrow(() -> new NotFoundException("Film with id " + id + " does not exist"));
        userStorage.getById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " does not exist"));

        film.getLikes().add(userId);
    }

    public void removeLike(Long id, Long userId) {
        Film film = getFilmById(id)
                .orElseThrow(() -> new NotFoundException("Film with id " + id + " does not exist"));
        userStorage.getById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " does not exist"));

        film.getLikes().remove(userId);
    }

    public Optional<Film> getMostLikedFilm() {
        return filmStorage.getAll().stream()
                .max(Comparator.comparingInt(f -> f.getLikes().size()));
    }

    public Collection<Film> getFilmsByLikes(int count) {
        return filmStorage.getAll().stream()
                .sorted(Comparator.comparingInt(f -> f.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList()).reversed();
    }
}
