package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmCreateDto;
import ru.yandex.practicum.filmorate.dto.FilmResponseDto;
import ru.yandex.practicum.filmorate.dto.FilmUpdateDto;
import ru.yandex.practicum.filmorate.dto.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @PostMapping
    public FilmResponseDto create(@Valid @RequestBody FilmCreateDto body) {
        return FilmMapper.toResponse(filmService.addNewFilm(FilmMapper.toFilm(body)));
    }

    @PutMapping
    public FilmResponseDto update(@Valid @RequestBody FilmUpdateDto body) {
        return FilmMapper.toResponse(filmService.updateFilm(body.getId(), FilmMapper.toFilm(body)));
    }

    @GetMapping
    public Collection<FilmResponseDto> findAll() {
        return filmService.getAllFilms().stream()
                .map(FilmMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public FilmResponseDto findById(@PathVariable Long id) {
        return FilmMapper.toResponse(
                filmService.getFilmById(id)
                        .orElseThrow(() -> new NotFoundException("Film with id " + id + " not found"))
        );
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
        return filmService.getFilmsByLikes(count).stream()
                .map(FilmMapper::toResponse)
                .collect(Collectors.toList());
    }
}
