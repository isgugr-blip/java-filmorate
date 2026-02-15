package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.AfterDate;

import java.util.Date;

@Data
@Builder(toBuilder = true)
public class Film {
    Long id;
    @NotBlank
    String name;
    @Size(max = 200)
    String description;
    @AfterDate("1895-12-28")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    Date releaseDate;
    @Min(1)
    int duration;
}
