package com.wildcodeschool.skillhub.repository;

import com.google.common.hash.Hashing;
import com.wildcodeschool.skillhub.entity.User;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UserRepository {

    private final static String DB_URL = "jdbc:mysql://localhost:3306/skillhub?serverTimezone=Europe/Paris";
    private final static String DB_USER = "skillhub";
    private final static String DB_PASSWORD = "gRMP!3_5hHVZKS-Z";

    private static Connection connection = null;

    private static void setConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public User getUserById(Long userId) {

        try {
            setConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM user JOIN picture ON picture.id_picture = user.id_picture " +
                            "WHERE id_user = ?;"
            );
            statement.setLong(1, userId);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            String username = resultSet.getString("nickname");
            String avatarUrl = resultSet.getString("url");
            String password = resultSet.getString("password");


            statement = connection.prepareStatement(
                    "SELECT user_skill.id_skill FROM skillhub.user\n" +
                            "JOIN user_skill ON user.id_user = user_skill.id_user\n" +
                            "WHERE user.id_user = ?;"
            );
            statement.setLong(1, userId);

            List<Long> skillsId = new ArrayList<>();
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                skillsId.add(resultSet.getLong("id_skill"));
            }

            return new User(userId, username, password, avatarUrl, skillsId);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Long checkUser(String username, String password) {

        password = Hashing.sha256()
                .hashString(password, StandardCharsets.UTF_8)
                .toString();

        try {
            setConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM user WHERE nickname = ? AND password = ?;"
            );
            statement.setString(1, username);
            statement.setString(2, password);
            Long userId = 0L;
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                userId = resultSet.getLong("id_user");
            }
            return userId;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean passwordCheck(String password, String passwordConfirmation) {

        return (password.equals(passwordConfirmation));
    }

    public boolean checkPasswordFormat(String password) {

        return ((password.length() >= 3) && (password.matches("[^0-9]*[0-9]+[^0-9]*")));
    }

    public void updateUser(Long userId, String nickname, String password, Long avatar, List<Integer> newSkills, List<Long> oldSkills) {

        try {
            setConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE user SET nickname=?, password=?, id_picture=? WHERE id_user=?;"
            );

            statement.setString(1, nickname);
            statement.setString(2, password);
            statement.setLong(3, avatar);
            statement.setLong(4, userId);

            if (statement.executeUpdate() != 1) {
                throw new SQLException("failed to update data");
            }

            for (Long oldSkill : oldSkills) {
                statement = connection.prepareStatement(
                        "DELETE FROM user_skill WHERE id_user=? AND id_skill=?;"
                );
                statement.setLong(1, userId);
                statement.setLong(2, oldSkill);
                if (statement.executeUpdate() != 1) {
                    throw new SQLException("failed to insert data");
                }
            }
            if (newSkills.get(0) != -1) {
                for (Integer skillId : newSkills) {
                    statement = connection.prepareStatement(
                            "INSERT INTO user_skill (id_skill, id_user) VALUES (?, ?);");
                    statement.setInt(1, skillId);
                    statement.setLong(2, userId);
                    if (statement.executeUpdate() != 1) {
                        throw new SQLException("failed to insert data");
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean checkExistingUsername(String username) {

        try {
            setConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT nickname FROM user WHERE nickname LIKE ? ;"
            );
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String testUsername = resultSet.getString("nickname");
                if (testUsername.equals(username)) {
                    return false;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }
}