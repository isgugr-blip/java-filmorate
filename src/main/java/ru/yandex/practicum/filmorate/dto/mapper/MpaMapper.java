package ru.yandex.practicum.filmorate.dto.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.model.Mpa;

@UtilityClass
public class MpaMapper {

    public static MpaDto toDto(Mpa mpa) {
        return MpaDto.builder()
                .id(mpa.getId())
                .name(mpa.getName())
                .build();
    }
}
