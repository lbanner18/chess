package client;

import org.junit.jupiter.api.*;
import server.Server;
import model.RegisterRequest;
import model.LoginRequest;
import model.CreateGameRequest;
import model.JoinGameRequest;
import model.ListGamesResult;
import model.GameData;
import server.Server;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);

        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @BeforeEach
    public void clearDatabase() {
        try {
            facade.clear();
        } catch (Exception e) {
            System.out.println("Failed to clear database: " + e.getMessage());
        }
    }

    @Test
    public void registerPositive() {
        var request = new RegisterRequest("player1", "password", "p1@email.com");

        assertDoesNotThrow(() -> {
            var result = facade.register(request);
            assertNotNull(result.authToken(), "AuthToken should not be null after successful registration");
            assertEquals("player1", result.username());
        });
    }

    @Test
    public void registerNegativeDuplicateUser() {
        var request = new RegisterRequest("player1", "password", "p1@email.com");

        assertDoesNotThrow(() -> facade.register(request));

        assertThrows(Exception.class, () -> facade.register(request),
                "Registering a duplicate user should throw an exception");
    }

    // --- LOGIN TESTS ---

    @Test
    public void loginPositive() throws Exception {
        facade.register(new RegisterRequest("player2", "pass", "p2@email.com"));

        var loginReq = new LoginRequest("player2", "pass");
        assertDoesNotThrow(() -> {
            var result = facade.login(loginReq);
            assertNotNull(result.authToken(), "AuthToken should be returned on successful login");
        });
    }

    @Test
    public void loginNegativeWrongPassword() throws Exception {
        facade.register(new RegisterRequest("player3", "pass", "p3@email.com"));

        var badLoginReq = new LoginRequest("player3", "wrongPassword");

        assertThrows(Exception.class, () -> facade.login(badLoginReq),
                "Logging in with the wrong password should throw an exception");
    }

    // --- CREATE GAME TESTS ---

    @Test
    public void createGamePositive() throws Exception {
        var auth = facade.register(new RegisterRequest("player4", "pass", "p4@email.com"));

        var gameReq = new CreateGameRequest("Test Game");
        assertDoesNotThrow(() -> {
            var result = facade.createGame(gameReq, auth.authToken());
            assertTrue(result.gameID() > 0, "Server should return a valid positive Game ID");
        });
    }

    @Test
    public void createGameNegativeBadToken() {
        var gameReq = new CreateGameRequest("Test Game");

        assertThrows(Exception.class, () -> facade.createGame(gameReq, "fake invalid token"),
                "Creating a game with a bad token should throw an exception");
    }

    // --- LIST GAMES TESTS ---

    @Test
    public void listGamesPositive() throws Exception {
        var auth = facade.register(new RegisterRequest("player6", "pass", "p6@email.com"));
        facade.createGame(new CreateGameRequest("List Test Game"), auth.authToken());

        assertDoesNotThrow(() -> {
            var result = facade.listGames(auth.authToken());
            assertNotNull(result.games(), "Games list should not be null");
            assertFalse(result.games().isEmpty(), "Games list should contain the newly created game");
        });
    }

    @Test
    public void listGamesNegativeBadToken() {
        assertThrows(Exception.class, () -> facade.listGames("gibberish"),
                "Listing games with an invalid token should throw an exception");
    }

    // --- JOIN GAME TESTS ---

    @Test
    public void joinGamePositive() throws Exception {
        var auth = facade.register(new RegisterRequest("player7", "pass", "p7@email.com"));
        var game = facade.createGame(new CreateGameRequest("Join Test Game"), auth.authToken());

        var joinReq = new JoinGameRequest("WHITE", game.gameID());

        assertDoesNotThrow(() -> facade.joinGame(joinReq, auth.authToken()),
                "Joining a valid game with an available color should succeed");
    }

    @Test
    public void joinGameNegativeAlreadyTaken() throws Exception {
        var auth1 = facade.register(new RegisterRequest("player8", "pass", "p8@email.com"));
        var auth2 = facade.register(new RegisterRequest("player9", "pass", "p9@email.com"));
        var game = facade.createGame(new CreateGameRequest("Taken Test Game"), auth1.authToken());

        var joinReq = new JoinGameRequest("WHITE", game.gameID());

        facade.joinGame(joinReq, auth1.authToken());

        assertThrows(Exception.class, () -> facade.joinGame(joinReq, auth2.authToken()),
                "Joining a game with a color that is already taken should throw an exception");
    }

    // --- LOGOUT TESTS ---

    @Test
    public void logoutPositive() throws Exception {
        var auth = facade.register(new RegisterRequest("player5", "pass", "p5@email.com"));

        assertDoesNotThrow(() -> facade.logout(auth.authToken()),
                "Logging out with a valid token should not throw an exception");
    }

    @Test
    public void logoutNegativeBadToken() {
        assertThrows(Exception.class, () -> facade.logout("idk man"),
                "Logging out with an invalid token should throw an exception");
    }
}
