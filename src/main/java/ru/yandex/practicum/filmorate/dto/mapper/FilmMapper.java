package ru.yandex.practicum.filmorate.dto.mapper;

import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.LinkedHashSet;
import java.util.stream.Collectors;

public final class FilmMapper {

    private FilmMapper() {
    }

    public static Film toFilm(FilmCreateDto dto) {
        Film.FilmBuilder builder = Film.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .releaseDate(dto.getReleaseDate())
                .duration(dto.getDuration());

        if (dto.getMpa() != null) {
            builder.mpa(Mpa.builder().id(dto.getMpa().getId()).build());
        }

        if (dto.getGenres() != null) {
            LinkedHashSet<Genre> genres = dto.getGenres().stream()
                    .map(g -> Genre.builder().id(g.getId()).build())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            builder.genres(genres);
        }

        return builder.build();
    }

    public static Film toFilm(FilmUpdateDto dto) {
        Film.FilmBuilder builder = Film.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .releaseDate(dto.getReleaseDate())
                .duration(dto.getDuration());

        if (dto.getMpa() != null) {
            builder.mpa(Mpa.builder().id(dto.getMpa().getId()).build());
        }

        if (dto.getGenres() != null) {
            LinkedHashSet<Genre> genres = dto.getGenres().stream()
                    .map(g -> Genre.builder().id(g.getId()).build())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            builder.genres(genres);
        }

        return builder.build();
    }

    public static FilmResponseDto toResponse(Film film) {
        FilmResponseDto.FilmResponseDtoBuilder builder = FilmResponseDto.builder()
                .id(film.getId())
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration());

        if (film.getMpa() != null) {
            builder.mpa(MpaDto.builder()
                    .id(film.getMpa().getId())
                    .name(film.getMpa().getName())
                    .build());
        }

        if (film.getGenres() != null) {
            LinkedHashSet<GenreDto> genres = film.getGenres().stream()
                    .map(g -> GenreDto.builder().id(g.getId()).name(g.getName()).build())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            builder.genres(genres);
        }

        return builder.build();
    }
}
