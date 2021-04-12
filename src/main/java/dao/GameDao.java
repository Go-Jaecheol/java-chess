package dao;

import domain.Board;
import domain.ChessGame;
import domain.piece.objects.Piece;
import domain.piece.objects.PieceFactory;
import domain.piece.position.Position;
import domain.state.Finished;
import domain.state.Running;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameDao {

    public Connection getConnection() {
        Connection con = null;
        String server = "localhost:13306";
        String database = "db_name";
        String option = "?useSSL=false&serverTimezone=UTC";
        String userName = "root";
        String password = "root";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println(" !! JDBC Driver load 오류: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            con = DriverManager.getConnection("jdbc:mysql://" + server + "/" + database + option, userName, password);
            System.out.println("정상적으로 연결되었습니다.");
        } catch (SQLException e) {
            System.err.println("연결 오류:" + e.getMessage());
            e.printStackTrace();
        }

        return con;
    }

    public void saveNewGame(ChessGame game) {
        saveGame(game);
        int gameId = lastGameID();
        savePieces(gameId, game);
    }

    private void saveGame(ChessGame game) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("insert into game(blackscore, whitescore, turn, isEnd) values(?, ?, ?, ?)")) {
            pstmt.setDouble(1, game.blackScore());
            pstmt.setDouble(2, game.whiteScore());
            pstmt.setBoolean(3, game.getTurn());
            pstmt.setBoolean(4, game.isEnd());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void savePieces(int gameId, ChessGame game) {
        Map<Position, Piece> pieceMap = game.getBoard().getPieceMap();
        pieceMap.entrySet()
                .forEach(entry -> insertPieceInfo(gameId, entry.getValue().name(), entry.getKey().toString()));
    }

    private void insertPieceInfo(int gameId, String name, String position) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("insert into piece(gameid, name, position) values(" + gameId + ", ?, ?)")) {
            pstmt.setString(1, name);
            pstmt.setString(2, position);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ChessGame move(int gameId, String source, String target) {
        ChessGame game = findGameById(gameId);
        game.move(Position.of(source), Position.of(target));
        updateGameInfo(gameId, game);
        deleteTargetPiece(gameId, target);
        updateTargetPiece(gameId, source, target);
        return game;
    }

    private void updateGameInfo(int gameId, ChessGame game) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("UPDATE game SET blackscore = ?, whitescore=?, turn=?, isEnd=? WHERE gameid=?")) {
            pstmt.setDouble(1, game.blackScore());
            pstmt.setDouble(2, game.whiteScore());
            pstmt.setBoolean(3, game.getTurn());
            pstmt.setBoolean(4, game.isEnd());
            pstmt.setInt(5, gameId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateTargetPiece(int gameId, String source, String target) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("update piece set position=? where position=? AND gameid=?")) {
            pstmt.setString(1, target);
            pstmt.setString(2, source);
            pstmt.setInt(3, gameId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteTargetPiece(int gameId, String target) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("delete from piece where position=? AND gameid=?");) {
            pstmt.setString(1, target);
            pstmt.setInt(2, gameId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ChessGame findGameById(int roomNumber) {
        boolean turn = false;
        boolean isEnd = false;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("select turn, isEnd from game where gameid=?")) {
            pstmt.setInt(1, roomNumber);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) throw new SQLException();
            turn = rs.getBoolean(1);
            isEnd = rs.getBoolean(2);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Map<Position, Piece> pieces = new HashMap<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("select name, position from piece where gameid=?")) {
            pstmt.setInt(1, roomNumber);
            ResultSet rs = pstmt.executeQuery();
            putResultSetToPieces(pieces, rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (isEnd) {
            return new ChessGame(new Finished(new Board(pieces), turn));
        }
        return new ChessGame(new Running(new Board(pieces), turn));
    }

    private void putResultSetToPieces(Map<Position, Piece> pieces, ResultSet rs) throws SQLException {
        while (rs.next()) {
            Piece piece = PieceFactory.findPiece(rs.getString(1));
            Position position = Position.of(rs.getString(2));
            pieces.put(position, piece);
        }
    }

    public void deleteGame(int roomNumber) {
        deleteInfo("piece", roomNumber);
        deleteInfo("game", roomNumber);
    }

    private void deleteInfo(String table, int roomNumber) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("delete from " + table + " where gameid=?")) {
            pstmt.setInt(1, roomNumber);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> findGames() {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("select gameid from game")) {
            ResultSet rs = pstmt.executeQuery();
            List<String> gameIDs = new ArrayList<>();
            while (rs.next()) {
                gameIDs.add(rs.getString(1));
            }
            return gameIDs;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public int lastGameID() {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT AUTO_INCREMENT FROM information_schema.tables WHERE table_name ='game' AND table_schema = DATABASE()")) {
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) throw new SQLException();
            return rs.getInt(1) - 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
