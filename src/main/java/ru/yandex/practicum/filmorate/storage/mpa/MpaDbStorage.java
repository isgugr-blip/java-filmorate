package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<Mpa> getById(Integer id) {
        List<Mpa> ratings = jdbcTemplate.query(
                "SELECT id, name FROM mpa_ratings WHERE id = ?",
                new MpaRowMapper(),
                id
        );
        return ratings.stream().findFirst();
    }

    @Override
    public Collection<Mpa> getAll() {
        return jdbcTemplate.query("SELECT id, name FROM mpa_ratings ORDER BY id", new MpaRowMapper());
    }

    private static class MpaRowMapper implements RowMapper<Mpa> {
        @Override
        public Mpa mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Mpa.builder()
                    .id(rs.getInt("id"))
                    .name(rs.getString("name"))
                    .build();
        }
    }
}
