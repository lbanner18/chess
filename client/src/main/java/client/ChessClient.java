package client;

import java.util.Arrays;
import model.RegisterRequest;
import model.LoginRequest;
import model.CreateGameRequest;
import model.JoinGameRequest;
import model.GameData;

public class ChessClient {
    private final ServerFacade server;
    private String authToken = null;
    private GameData[] savedGames = new GameData[0];

    public ChessClient(int serverPort) {
        server = new ServerFacade(serverPort);
    }

    public boolean isLoggedIn() {
        return authToken != null;
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);

            if (!isLoggedIn()) {
                return evalPreLogin(cmd, params);
            } else {
                return evalPostLogin(cmd, params);
            }
        } catch (ResponseException exception) {
            return exception.getMessage();
        }
    }

    private String evalPreLogin(String cmd, String[] params) throws ResponseException {
        return switch (cmd) {
            case "register" -> register(params);
            case "login" -> login(params);
            case "quit" -> "quit";
            default -> helpPreLogin();
        };
    }

    private String evalPostLogin(String cmd, String[] params) throws ResponseException {
        return switch (cmd) {
            case "create" -> createGame(params);
            case "list" -> listGames();
            case "join" -> joinGame(params);
            case "observe" -> observeGame(params);
            case "logout" -> logout();
            default -> helpPostLogin();
        };
    }

    //Pre-Login

    private String register(String[] params) throws ResponseException {
        if (params.length == 3) {
            var request = new RegisterRequest(params[0], params[1], params[2]);
            var result = server.register(request);
            authToken = result.authToken();

            var listResult = server.listGames(authToken);
            savedGames = listResult.games().toArray(new GameData[0]);

            return String.format("Successfully registered and logged in as %s.", result.username());
        }
        return "Expected syntax: register <username> <password> <email>";
    }

    private String login(String[] params) throws ResponseException {
        if (params.length == 2) {
            var request = new LoginRequest(params[0], params[1]);
            var result = server.login(request);
            authToken = result.authToken();

            var listResult = server.listGames(authToken);
            savedGames = listResult.games().toArray(new GameData[0]);

            return String.format("Successfully logged in as %s.", result.username());
        }
        return "Expected syntax: login <username> <password>";
    }

    private String helpPreLogin() {
        return """
                Available Commands:
                - register <username> <password> <email> - Create an account
                - login <username> <password> - Login to play and observe Chess
                - quit - Exit CL Chess
                - help - List available commands
                """;
    }

    //Post-Login

    private String createGame(String[] params) throws ResponseException {
        if (params.length == 1) {
            var request = new CreateGameRequest(params[0]);
            server.createGame(request, authToken);
            return String.format("Game '%s' created successfully!", params[0]);
        }
        return "Expected Syntax: create <NAME>";
    }

    private String listGames() throws ResponseException {
        var result = server.listGames(authToken);

        savedGames = result.games().toArray(new GameData[0]);

        StringBuilder sb = new StringBuilder();
        sb.append("Available Games:\n");
        for (int i = 0; i < savedGames.length; i++) {
            GameData game = savedGames[i];
            String white = game.whiteUsername() != null ? game.whiteUsername() : "Empty";
            String black = game.blackUsername() != null ? game.blackUsername() : "Empty";

            sb.append(String.format("  %d. %S (White: %s, Black: %s)\n",
                    i + 1, game.gameName(), white, black));
        }
        return sb.toString();
    }

    private String joinGame(String[] params) throws ResponseException {
        if (params.length == 2) {
            try {
                int index = Integer.parseInt(params[0]) - 1;
                if (index < 0 || index >= savedGames.length) {
                    return "Invalid game ID. Run 'list' to see available games.";
                }

                int gameID = savedGames[index].gameID();
                String color = params[1].toUpperCase();

                var request = new JoinGameRequest(color, gameID);
                server.joinGame(request, authToken);

                return String.format("Successfully joined game %d as %s.", (index + 1), color);
            } catch (NumberFormatException ex) {
                return "Expected a number for the game ID.";
            }
        }
        return "Expected syntax: join <ID> [WHITE]/[BLACK]";
    }

    private String observeGame(String[] params) throws ResponseException {
        if (params.length == 1) {
            try {
                int index = Integer.parseInt(params[0]) - 1;
                if (index < 0 || index >= savedGames.length) {
                    return "Invalid game ID. Run 'list' to see available games.";
                }

                int gameID = savedGames[index].gameID();

                var request = new JoinGameRequest(null, gameID);
                server.joinGame(request, authToken);

                return String.format("Now observing game %d. ", (index + 1));
            } catch (NumberFormatException e) {
                return "Expected a number for the game ID.";
            }
        }
        return "Expected: observe <ID>";
    }

    private String logout() throws ResponseException {
        server.logout(authToken);
        authToken = null;
        savedGames = new GameData[0];
        return "Logged out successfully.";
    }

    private String helpPostLogin() {
        return """
               Available Commands:
               - create <NAME> - Create a game
               - list - Lists available games
               - join <ID> [WHITE]/[BLACK] - Join a game as White or Black
               - observe <ID> - Observe a game
               - logout - Log out of CL Chess
               - quit - Exit CL Chess
               - help - List available commands
               """;
    }
}
