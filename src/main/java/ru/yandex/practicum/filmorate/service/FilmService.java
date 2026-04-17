package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmCreateDto;
import ru.yandex.practicum.filmorate.dto.FilmResponseDto;
import ru.yandex.practicum.filmorate.dto.FilmUpdateDto;
import ru.yandex.practicum.filmorate.dto.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    public FilmResponseDto addNewFilm(FilmCreateDto dto) {
        Film film = FilmMapper.toFilm(dto);
        validateMpaAndGenres(film);
        return FilmMapper.toResponse(filmStorage.create(film));
    }

    public FilmResponseDto updateFilm(FilmUpdateDto dto) {
        if (!filmStorage.existsById(dto.getId())) {
            throw new NotFoundException("Film with id " + dto.getId() + " does not exist");
        }
        Film film = FilmMapper.toFilm(dto);
        validateMpaAndGenres(film);
        return FilmMapper.toResponse(filmStorage.update(dto.getId(), film));
    }

    public FilmResponseDto getFilmById(Long id) {
        Film film = filmStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Film with id " + id + " not found"));
        return FilmMapper.toResponse(film);
    }

    public Collection<FilmResponseDto> getAllFilms() {
        return filmStorage.getAll().stream()
                .map(FilmMapper::toResponse)
                .collect(Collectors.toList());
    }

    public void addLike(Long filmId, Long userId) {
        if (!filmStorage.existsById(filmId)) {
            throw new NotFoundException("Film with id " + filmId + " does not exist");
        }
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("User with id " + userId + " does not exist");
        }
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        if (!filmStorage.existsById(filmId)) {
            throw new NotFoundException("Film with id " + filmId + " does not exist");
        }
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("User with id " + userId + " does not exist");
        }
        filmStorage.removeLike(filmId, userId);
    }

    public Collection<FilmResponseDto> getFilmsByLikes(int count) {
        return filmStorage.getPopularFilms(count).stream()
                .map(FilmMapper::toResponse)
                .collect(Collectors.toList());
    }

    private void validateMpaAndGenres(Film film) {
        if (film.getMpa() != null) {
            mpaStorage.getById(film.getMpa().getId())
                    .orElseThrow(() -> new NotFoundException(
                            "MPA rating with id " + film.getMpa().getId() + " not found"));
        }
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                genreStorage.getById(genre.getId())
                        .orElseThrow(() -> new NotFoundException(
                                "Genre with id " + genre.getId() + " not found"));
            }
        }
    }
}
