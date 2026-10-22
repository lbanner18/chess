package service;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {
    private GameDAO gameDAO;
    private AuthDAO authDAO;
    private UserDAO userDAO;
    private GameService gameService;
    private UserService userService;

    private String validAuthToken;

    @BeforeEach
    public void setup() throws DataAccessException {
        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();
        userDAO = new MemoryUserDAO();

        gameService = new GameService(gameDAO, authDAO);
        userService = new UserService(userDAO, authDAO);

        RegisterResult regResult = userService.register(new RegisterRequest("Luke", "password", "luke@email.com"));
        validAuthToken = regResult.authToken();
    }

    // --- CREATE GAME TESTS ---

    @Test
    public void createGameSuccess() throws DataAccessException {
        CreateGameResult result = gameService.createGame(validAuthToken, new CreateGameRequest("My First Game"));

        assertNotNull(result);
        assertTrue(result.gameID() > 0);
        assertEquals(1, gameDAO.listGames().size());
    }

    @Test
    public void createGameUnauthorizedFails() {
        CreateGameRequest request = new CreateGameRequest("Sneaky Game");

        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            gameService.createGame("fake-invalid-token", request);
        });
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    // --- LIST GAMES TESTS ---

    @Test
    public void listGamesSuccess() throws DataAccessException {
        // Create 3 games
        gameService.createGame(validAuthToken, new CreateGameRequest("Game 1"));
        gameService.createGame(validAuthToken, new CreateGameRequest("Game 2"));
        gameService.createGame(validAuthToken, new CreateGameRequest("Game 3"));

        ListGamesResult result = gameService.listGames(validAuthToken);

        assertNotNull(result.games());
        assertEquals(3, result.games().size());
    }

    @Test
    public void listGamesUnauthorizedFails() {
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            gameService.listGames("fake-invalid-token");
        });
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    // --- JOIN GAME TESTS ---

    @Test
    public void joinGameSuccess() throws DataAccessException {
        CreateGameResult createResult = gameService.createGame(validAuthToken, new CreateGameRequest("Match 1"));
        int gameID = createResult.gameID();

        JoinGameRequest joinRequest = new JoinGameRequest("WHITE", gameID);
        gameService.joinGame(validAuthToken, joinRequest);

        GameData updatedGame = gameDAO.getGame(gameID);
        assertEquals("Luke", updatedGame.whiteUsername());
        assertNull(updatedGame.blackUsername());
    }

    @Test
    public void joinGameColorAlreadyTakenFails() throws DataAccessException {
        CreateGameResult createResult = gameService.createGame(validAuthToken, new CreateGameRequest("Match 1"));
        gameService.joinGame(validAuthToken, new JoinGameRequest("WHITE", createResult.gameID()));

        RegisterResult user2Reg = userService.register(new RegisterRequest("Vader", "darkside", "sith@empire.com"));
        
        JoinGameRequest stealRequest = new JoinGameRequest("WHITE", createResult.gameID());

        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(user2Reg.authToken(), stealRequest);
        });
        assertEquals("Error: already taken", exception.getMessage());
    }

    @Test
    public void joinGameBadIdFails() {
        JoinGameRequest badRequest = new JoinGameRequest("BLACK", 9999);

        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(validAuthToken, badRequest);
        });
        assertEquals("Error: bad request", exception.getMessage());
    }
}