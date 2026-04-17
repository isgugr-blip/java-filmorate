package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film create(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO films (name, description, release_date, duration, mpa_rating_id) " +
                            "VALUES (?, ?, ?, ?, ?)",
                    new String[]{"id"}
            );
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            if (film.getMpa() != null) {
                ps.setInt(5, film.getMpa().getId());
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }
            return ps;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        saveGenres(film);

        return getById(film.getId()).orElse(film);
    }

    @Override
    public Film update(Long id, Film film) {
        jdbcTemplate.update(
                "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? " +
                        "WHERE id = ?",
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                id
        );

        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", id);
        film.setId(id);
        saveGenres(film);

        return getById(id).orElse(film);
    }

    @Override
    public boolean existsById(Long id) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM films WHERE id = ?", Integer.class, id
        );
        return count != null && count > 0;
    }

    @Override
    public Optional<Film> getById(Long id) {
        List<Film> films = jdbcTemplate.query(
                "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
                        "f.mpa_rating_id, m.name AS mpa_name " +
                        "FROM films f " +
                        "LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
                        "WHERE f.id = ?",
                new FilmRowMapper(),
                id
        );

        if (films.isEmpty()) {
            return Optional.empty();
        }

        Film film = films.getFirst();
        loadGenres(List.of(film));
        loadLikes(List.of(film));
        return Optional.of(film);
    }

    @Override
    public Collection<Film> getAll() {
        List<Film> films = jdbcTemplate.query(
                "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
                        "f.mpa_rating_id, m.name AS mpa_name " +
                        "FROM films f " +
                        "LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id",
                new FilmRowMapper()
        );

        loadGenres(films);
        loadLikes(films);
        return films;
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM films WHERE id = ?", id);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        jdbcTemplate.update(
                "MERGE INTO film_likes (film_id, user_id) VALUES (?, ?)",
                filmId, userId
        );
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        jdbcTemplate.update(
                "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?",
                filmId, userId
        );
    }

    @Override
    public Collection<Film> getPopularFilms(int count) {
        List<Film> films = jdbcTemplate.query(
                "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
                        "f.mpa_rating_id, m.name AS mpa_name " +
                        "FROM films f " +
                        "LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
                        "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                        "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, " +
                        "f.mpa_rating_id, m.name " +
                        "ORDER BY COUNT(fl.user_id) DESC " +
                        "LIMIT ?",
                new FilmRowMapper(),
                count
        );

        loadGenres(films);
        loadLikes(films);
        return films;
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update(
                    "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                    film.getId(), genre.getId()
            );
        }
    }

    private void loadGenres(List<Film> films) {
        if (films.isEmpty()) return;

        Map<Long, Film> filmMap = films.stream()
                .collect(Collectors.toMap(Film::getId, f -> f));

        String ids = filmMap.keySet().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        jdbcTemplate.query(
                "SELECT fg.film_id, g.id, g.name FROM film_genres fg " +
                        "JOIN genres g ON fg.genre_id = g.id " +
                        "WHERE fg.film_id IN (" + ids + ") " +
                        "ORDER BY g.id",
                rs -> {
                    Long filmId = rs.getLong("film_id");
                    Genre genre = Genre.builder()
                            .id(rs.getInt("id"))
                            .name(rs.getString("name"))
                            .build();
                    Film film = filmMap.get(filmId);
                    if (film != null) {
                        film.getGenres().add(genre);
                    }
                }
        );
    }

    private void loadLikes(List<Film> films) {
        if (films.isEmpty()) return;

        Map<Long, Film> filmMap = films.stream()
                .collect(Collectors.toMap(Film::getId, f -> f));

        String ids = filmMap.keySet().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        jdbcTemplate.query(
                "SELECT film_id, user_id FROM film_likes WHERE film_id IN (" + ids + ")",
                rs -> {
                    Long filmId = rs.getLong("film_id");
                    Long userId = rs.getLong("user_id");
                    Film film = filmMap.get(filmId);
                    if (film != null) {
                        film.getLikes().add(userId);
                    }
                }
        );
    }

    private static class FilmRowMapper implements RowMapper<Film> {
        @Override
        public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
            Film.FilmBuilder builder = Film.builder()
                    .id(rs.getLong("id"))
                    .name(rs.getString("name"))
                    .description(rs.getString("description"))
                    .releaseDate(rs.getDate("release_date").toLocalDate())
                    .duration(rs.getInt("duration"));

            int mpaId = rs.getInt("mpa_rating_id");
            if (!rs.wasNull()) {
                builder.mpa(Mpa.builder()
                        .id(mpaId)
                        .name(rs.getString("mpa_name"))
                        .build());
            }

            return builder.build();
        }
    }
}
