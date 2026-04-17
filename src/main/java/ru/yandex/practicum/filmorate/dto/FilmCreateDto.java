package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.AfterDate;

import java.time.LocalDate;
import java.util.Set;

@Data
public class FilmCreateDto {
    @NotBlank
    private String name;
    @Size(max = 200)
    private String description;
    @AfterDate("1895-12-28")
    private LocalDate releaseDate;
    @Min(1)
    private int duration;
    private MpaDto mpa;
    private Set<GenreDto> genres;
}
