package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreStorage genreStorage;

    public GenreDto getById(Integer id) {
        return GenreMapper.toDto(
                genreStorage.getById(id)
                        .orElseThrow(() -> new NotFoundException("Genre with id " + id + " not found"))
        );
    }

    public Collection<GenreDto> getAll() {
        return genreStorage.getAll().stream()
                .map(GenreMapper::toDto)
                .collect(Collectors.toList());
    }
}
