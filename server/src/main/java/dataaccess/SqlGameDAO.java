package dataaccess;

import model.GameData;
import chess.ChessGame;
import com.google.gson.Gson;

import java.sql.SQLException;
import java.util.Collection;
import java.util.ArrayList;
import java.sql.Statement;

public class SqlGameDAO implements GameDAO {

    public SqlGameDAO() throws DataAccessException {
        configureDatabase();
    }

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            var createTableSql = """
                    CREATE TABLE IF NOT EXISTS gameData (
                        gameID INT NOT NULL AUTO_INCREMENT,
                        whiteUsername VARCHAR(255),
                        blackUsername VARCHAR(255),
                        gameName VARCHAR(255) NOT NULL,
                        game TEXT NOT NULL,
                        PRIMARY KEY (gameID)
                    )
                """;
            try (var preparedStatement = conn.prepareStatement(createTableSql)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to configure database: %s", e.getMessage()));
        }
    }

    @Override
    public int createGame(String gameName) throws DataAccessException {
        String statement = "INSERT INTO gameData (whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?)";

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, null);
            ps.setString(2, null);
            ps.setString(3, gameName);

            ChessGame newGame = new ChessGame();
            var json = new Gson().toJson(newGame);
            ps.setString(4, json);

            ps.executeUpdate();

            try (var rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: unable to create game: " + e.getMessage());
        }
        return 0;
    }


    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        String statement = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM gameData WHERE gameID = ?";

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {

            ps.setInt(1, gameID);

            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Pull the JSON string out of the database and convert it back to a ChessGame object
                    var json = rs.getString("game");
                    var chessGame = new Gson().fromJson(json, ChessGame.class);

                    return new GameData(
                            rs.getInt("gameID"),
                            rs.getString("whiteUsername"),
                            rs.getString("blackUsername"),
                            rs.getString("gameName"),
                            chessGame
                    );
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: unable to read game: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        var games = new ArrayList<GameData>();
        String statement = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM gameData";

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement);
             var rs = ps.executeQuery()) {

            while (rs.next()) {
                var json = rs.getString("game");
                var chessGame = new Gson().fromJson(json, ChessGame.class);

                games.add(new GameData(
                        rs.getInt("gameID"),
                        rs.getString("whiteUsername"),
                        rs.getString("blackUsername"),
                        rs.getString("gameName"),
                        chessGame
                ));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: unable to list games: " + e.getMessage());
        }
        return games;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        String statement = "UPDATE gameData SET whiteUsername = ?, blackUsername = ?, gameName = ?, game = ? WHERE gameID = ?";

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {

            ps.setString(1, game.whiteUsername());
            ps.setString(2, game.blackUsername());
            ps.setString(3, game.gameName());

            var json = new Gson().toJson(game.game());
            ps.setString(4, json);

            ps.setInt(5, game.gameID());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Error: unable to update game: " + e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String statement = "TRUNCATE TABLE gameData";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error: unable to clear game data: " + e.getMessage());
        }
    }
}
