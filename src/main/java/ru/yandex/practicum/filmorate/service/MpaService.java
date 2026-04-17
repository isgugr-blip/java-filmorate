package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.dto.mapper.MpaMapper;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaStorage mpaStorage;

    public MpaDto getById(Integer id) {
        return MpaMapper.toDto(
                mpaStorage.getById(id)
                        .orElseThrow(() -> new NotFoundException("MPA rating with id " + id + " not found"))
        );
    }

    public Collection<MpaDto> getAll() {
        return mpaStorage.getAll().stream()
                .map(MpaMapper::toDto)
                .collect(Collectors.toList());
    }
}
