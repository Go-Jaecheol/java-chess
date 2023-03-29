package chess.dao;

import chess.domain.board.Turn;
import chess.domain.piece.Color;
import chess.dto.GameDto;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameDao {
    private static final String SERVER = "localhost:13306"; // MySQL 서버 주소
    private static final String DATABASE = "chess"; // MySQL DATABASE 이름
    private static final String OPTION = "?useSSL=false&serverTimezone=UTC";
    private static final String USERNAME = "root"; //  MySQL 서버 아이디
    private static final String PASSWORD = "root"; // MySQL 서버 비밀번호

    public Connection getConnection() {
        // 드라이버 연결
        try {
            return DriverManager.getConnection("jdbc:mysql://" + SERVER + "/" + DATABASE + OPTION, USERNAME, PASSWORD);
        } catch (final SQLException e) {
            System.err.println("DB 연결 오류:" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void create(final GameDto gameDto) {
        final String query = "INSERT INTO game VALUES (?, ?, ?)";
        final int id = autoIncrementId();
        try (final var connection = getConnection();
             final var preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            preparedStatement.setString(2, gameDto.getTurn());
            preparedStatement.setBoolean(3, gameDto.getIsRunning());
            preparedStatement.executeUpdate();
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Integer> findAllIds() {
        String query = "SELECT id FROM game";
        List<Integer> runningGameIds = new ArrayList<>();
        try (final var connection = getConnection();
             final var preparedStatement = connection.prepareStatement(query)) {
            final ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                runningGameIds.add(resultSet.getInt("id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return runningGameIds;
    }

    public Turn findTurnById(final int id) {
        String query = "SELECT turn FROM game WHERE id = ?";
        try (final var connection = getConnection();
             final var preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new Turn(Color.ofString(resultSet.getString("turn")));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private int autoIncrementId() {
        List<Integer> gameIds = findAllIds();
        if (gameIds.isEmpty()) {
            return 1;
        }
        return Collections.max(gameIds) + 1;
    }

    public void update(final GameDto gameDto) {
        final String query = "UPDATE game SET turn = ? WHERE id = ?";
        final int id = 1;
        try (final var connection = getConnection();
             final var preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, gameDto.getTurn());
            preparedStatement.setInt(2, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(final int id) {
        final String query = "DELETE FROM game WHERE id = ?";
        try (final var connection = getConnection();
             final var preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
