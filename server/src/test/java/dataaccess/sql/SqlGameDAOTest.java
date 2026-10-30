package dataaccess.sql;

import dataaccess.DataAccessException;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class SqlGameDAOTest {

    private SqlGameDAO gameDAO;

    @BeforeEach
    public void setUp() throws DataAccessException {
        gameDAO = new SqlGameDAO();
        gameDAO.clear();
    }

    @Test
    @DisplayName("createGame() Positive: Successfully insert and retrieve a game ID")
    public void createGamePositive() throws DataAccessException {
        int gameID = gameDAO.createGame("Epic Chess Match");
        assertTrue(gameID > 0, "Game ID should be a positive auto-generated integer");
    }

    @Test
    @DisplayName("createGame() Negative: Throws exception on null game name")
    public void createGameNegative() {
        assertThrows(DataAccessException.class, () -> {
            gameDAO.createGame(null);
        }, "Inserting a null game name should throw a DataAccessException");
    }

    @Test
    @DisplayName("getGame() Positive: Retrieve an existing game with a valid ChessGame object")
    public void getGamePositive() throws DataAccessException {
        int gameID = gameDAO.createGame("Test Game");

        GameData retrievedGame = gameDAO.getGame(gameID);

        assertNotNull(retrievedGame, "Game should be found in the database");
        assertEquals("Test Game", retrievedGame.gameName(), "Game names should match");
        assertNotNull(retrievedGame.game(), "The Gson serialization should have recreated the ChessGame object");
    }

    @Test
    @DisplayName("getGame() Negative: Return null for missing game ID")
    public void getGameNegative() throws DataAccessException {
        GameData found = gameDAO.getGame(999999); // ID that doesn't exist
        assertNull(found, "Searching for a non-existent game ID should return null");
    }

    @Test
    @DisplayName("listGames() Positive: Retrieve all created games")
    public void listGamesPositive() throws DataAccessException {
        gameDAO.createGame("Game 1");
        gameDAO.createGame("Game 2");
        gameDAO.createGame("Game 3");

        Collection<GameData> games = gameDAO.listGames();

        assertEquals(3, games.size(), "Should retrieve exactly 3 games");
    }

    @Test
    @DisplayName("updateGame() Positive: Successfully modify usernames and save state")
    public void updateGamePositive() throws DataAccessException {
        int gameID = gameDAO.createGame("My Game");
        GameData originalGame = gameDAO.getGame(gameID);

        GameData updatedGame = new GameData(
                originalGame.gameID(),
                "LukeBanner",
                originalGame.blackUsername(),
                originalGame.gameName(),
                originalGame.game()
        );

        gameDAO.updateGame(updatedGame);

        GameData verifyGame = gameDAO.getGame(gameID);
        assertEquals("LukeBanner", verifyGame.whiteUsername(), "White username should be updated to LukeBanner");
    }

    @Test
    @DisplayName("clear() Positive: Remove all games")
    public void clearPositive() throws DataAccessException {
        gameDAO.createGame("Doomed Game 1");
        gameDAO.createGame("Doomed Game 2");

        gameDAO.clear();
        Collection<GameData> games = gameDAO.listGames();

        assertTrue(games.isEmpty(), "List of games should be empty after clear() is called");
    }

    @Test
    @DisplayName("listGames() Positive: Returns empty collection when no games exist")
    public void listGamesEmpty() throws DataAccessException {
        gameDAO.clear();

        var games = gameDAO.listGames();
        assertTrue(games.isEmpty(), "List of games should be empty when no games have been created");
    }

    @Test
    @DisplayName("listGames() Negative: Throws exception on database error")
    public void listGamesException() throws Exception {
        try (var conn = dataaccess.DatabaseManager.getConnection();
             var ps = conn.prepareStatement("DROP TABLE gameData")) {
            ps.executeUpdate();
        }

        assertThrows(DataAccessException.class, () -> {
            gameDAO.listGames();
        });
    }
}