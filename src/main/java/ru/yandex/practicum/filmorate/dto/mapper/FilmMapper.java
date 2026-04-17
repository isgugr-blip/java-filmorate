package ru.yandex.practicum.filmorate.dto.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.filmorate.dto.FilmCreateDto;
import ru.yandex.practicum.filmorate.dto.FilmResponseDto;
import ru.yandex.practicum.filmorate.dto.FilmUpdateDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.LinkedHashSet;
import java.util.stream.Collectors;

@UtilityClass
public class FilmMapper {

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
            builder.mpa(MpaMapper.toDto(film.getMpa()));
        }

        if (film.getGenres() != null) {
            LinkedHashSet<GenreDto> genres = film.getGenres().stream()
                    .map(GenreMapper::toDto)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            builder.genres(genres);
        }

        return builder.build();
    }
}
