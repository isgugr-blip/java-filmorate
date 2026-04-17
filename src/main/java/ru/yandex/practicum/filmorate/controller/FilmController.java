package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmCreateDto;
import ru.yandex.practicum.filmorate.dto.FilmResponseDto;
import ru.yandex.practicum.filmorate.dto.FilmUpdateDto;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @PostMapping
    public FilmResponseDto create(@Valid @RequestBody FilmCreateDto body) {
        return filmService.addNewFilm(body);
    }

    @PutMapping
    public FilmResponseDto update(@Valid @RequestBody FilmUpdateDto body) {
        return filmService.updateFilm(body);
    }

    @GetMapping
    public Collection<FilmResponseDto> findAll() {
        return filmService.getAllFilms();
    }

    @GetMapping("/{id}")
    public FilmResponseDto findById(@PathVariable Long id) {
        return filmService.getFilmById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public Collection<FilmResponseDto> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        return filmService.getFilmsByLikes(count);
    }
}
