package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    public Film create(Film film);
    public Film update(Long id, Film film);
    public Optional<Film> getById(Long id);
    public Collection<Film> getAll();
    public void delete(Long id);
}
