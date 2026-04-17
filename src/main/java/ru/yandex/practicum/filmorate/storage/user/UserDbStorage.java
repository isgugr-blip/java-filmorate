package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public User create(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                    new String[]{"id"}
            );
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return user;
    }

    @Override
    public User update(Long id, User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        jdbcTemplate.update(
                "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?",
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                id
        );

        user.setId(id);
        return getById(id).orElse(user);
    }

    @Override
    public boolean existsById(Long id) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE id = ?", Integer.class, id
        );
        return count != null && count > 0;
    }

    @Override
    public Optional<User> getById(Long id) {
        List<User> users = jdbcTemplate.query(
                "SELECT id, email, login, name, birthday FROM users WHERE id = ?",
                new UserRowMapper(),
                id
        );

        if (users.isEmpty()) {
            return Optional.empty();
        }

        User user = users.getFirst();
        loadFriends(user);
        return Optional.of(user);
    }

    @Override
    public Collection<User> getAllUsers() {
        return jdbcTemplate.query(
                "SELECT id, email, login, name, birthday FROM users",
                new UserRowMapper()
        );
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        jdbcTemplate.update(
                "MERGE INTO friendships (user_id, friend_id, status) VALUES (?, ?, 'UNCONFIRMED')",
                userId, friendId
        );

        Integer reverseCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ?",
                Integer.class, friendId, userId
        );
        if (reverseCount != null && reverseCount > 0) {
            jdbcTemplate.update(
                    "UPDATE friendships SET status = 'CONFIRMED' " +
                            "WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)",
                    userId, friendId, friendId, userId
            );
        }
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        jdbcTemplate.update(
                "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?",
                userId, friendId
        );

        jdbcTemplate.update(
                "UPDATE friendships SET status = 'UNCONFIRMED' " +
                        "WHERE user_id = ? AND friend_id = ?",
                friendId, userId
        );
    }

    @Override
    public Collection<User> getFriends(Long userId) {
        return jdbcTemplate.query(
                "SELECT u.id, u.email, u.login, u.name, u.birthday " +
                        "FROM users u " +
                        "JOIN friendships f ON u.id = f.friend_id " +
                        "WHERE f.user_id = ?",
                new UserRowMapper(),
                userId
        );
    }

    @Override
    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        return jdbcTemplate.query(
                "SELECT u.id, u.email, u.login, u.name, u.birthday " +
                        "FROM users u " +
                        "JOIN friendships f1 ON u.id = f1.friend_id " +
                        "JOIN friendships f2 ON u.id = f2.friend_id " +
                        "WHERE f1.user_id = ? AND f2.user_id = ?",
                new UserRowMapper(),
                userId, otherId
        );
    }

    private void loadFriends(User user) {
        List<Long> friendIds = jdbcTemplate.query(
                "SELECT friend_id FROM friendships WHERE user_id = ?",
                (rs, rowNum) -> rs.getLong("friend_id"),
                user.getId()
        );
        user.setFriends(new HashSet<>(friendIds));
    }

    private static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            return User.builder()
                    .id(rs.getLong("id"))
                    .email(rs.getString("email"))
                    .login(rs.getString("login"))
                    .name(rs.getString("name"))
                    .birthday(rs.getDate("birthday").toLocalDate())
                    .build();
        }
    }
}
