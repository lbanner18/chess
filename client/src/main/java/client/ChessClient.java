package client;

import java.util.Arrays;
import model.RegisterRequest;
import model.LoginRequest;
import model.CreateGameRequest;
import model.JoinGameRequest;
import model.GameData;
import chess.ChessBoard;
import ui.BoardPrinter;

public class ChessClient {
    private final ServerFacade server;
    private String authToken = null;
    private GameData[] savedGames = new GameData[0];

    //The facade connects the server to the brain. The facade starts up here
    public ChessClient(int serverPort) {
        server = new ServerFacade(serverPort);
    }

    public boolean isLoggedIn() {
        return authToken != null;
    }

    //Gets the string and separates it into its distinct parts. # of parameters are stored for future command checks
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

    //In Pre-Login state, reads the first command and uses case-switch to determine next step. Defaults back to help
    private String evalPreLogin(String cmd, String[] params) throws ResponseException {
        return switch (cmd) {
            case "register" -> register(params);
            case "login" -> login(params);
            case "quit" -> "quit";
            default -> helpPreLogin();
        };
    }

    //Same as above but for Post-Login state
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

    //Register command combines inputs into server's register method's expected argument and runs server.register()
    private String register(String[] params) throws ResponseException {
        if (params.length == 3) {
            var request = new RegisterRequest(params[0], params[1], params[2]);
            var result = server.register(request);
            authToken = result.authToken();

            //Refreshes saved games so newly registered players can immediately access saved games w/o running list
            var listResult = server.listGames(authToken);
            savedGames = listResult.games().toArray(new GameData[0]);

            return String.format("Successfully registered and logged in as %s.", result.username());
        }
        return "Expected syntax: register <username> <password> <email>";
    }

    //Login command combines inputs into server's login method's expected argument and runs server.login()
    private String login(String[] params) throws ResponseException {
        if (params.length == 2) {
            var request = new LoginRequest(params[0], params[1]);
            var result = server.login(request);
            authToken = result.authToken();

            //Refreshes saved games so newly logged in players can immediately access saved games w/o running list
            var listResult = server.listGames(authToken);
            savedGames = listResult.games().toArray(new GameData[0]);

            return String.format("Successfully logged in as %s.", result.username());
        }
        return "Expected syntax: login <username> <password>";
    }

    //Outputs the help menu. Meant to be simple enough for a grandma
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

    //Same as Pre-login, take createGame command and format arguments to fit server.createGame()
    private String createGame(String[] params) throws ResponseException {
        if (params.length == 1) {
            var request = new CreateGameRequest(params[0]);
            server.createGame(request, authToken);

            var listResult = server.listGames(authToken);
            savedGames = listResult.games().toArray(new GameData[0]);

            return String.format("Game '%s' created successfully!", params[0]);
        }
        return "Expected Syntax: create <NAME>";
    }

    //Lists games in database
    private String listGames() throws ResponseException {
        var result = server.listGames(authToken);

        savedGames = result.games().toArray(new GameData[0]);

        StringBuilder sb = new StringBuilder();
        sb.append("Available Games:\n");
        //Lists which players are available
        for (int i = 0; i < savedGames.length; i++) {
            GameData game = savedGames[i];
            String white = game.whiteUsername() != null ? game.whiteUsername() : "Empty";
            String black = game.blackUsername() != null ? game.blackUsername() : "Empty";

            sb.append(String.format("  %d. %S (White: %s, Black: %s)\n",
                    i + 1, game.gameName(), white, black));
        }
        return sb.toString();
    }

    //Connects a user to a game and outputs board. Takes color argument for JoinGameRequest() arguments
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

                //Creates new chessboard in database, resets, and prints
                ChessBoard board = new ChessBoard();
                board.resetBoard();

                BoardPrinter.printBoard(board, color.equals("WHITE"));

                return String.format("Successfully joined game %d as %s.", (index + 1), color);
            } catch (NumberFormatException ex) {
                return "Expected a number for the game ID.";
            }
        }
        return "Expected syntax: join <ID> [WHITE]/[BLACK]";
    }

    //Games are listed and the board of a saved game is output so the user can observe but not modify the board.
    private String observeGame(String[] params) throws ResponseException {
        if (params.length == 1) {
            try {
                int index = Integer.parseInt(params[0]) - 1;
                if (index < 0 || index >= savedGames.length) {
                    return "Invalid game ID. Run 'list' to see available games.";
                }

                GameData game = savedGames[index];

                ChessBoard board = game.game().getBoard();

                BoardPrinter.printBoard(board, true);

                return String.format("Now observing game %d. ", (index + 1));
            } catch (NumberFormatException e) {
                return "Expected a number for the game ID.";
            }
        }
        return "Expected: observe <ID>";
    }

    //Logs out user
    private String logout() throws ResponseException {
        server.logout(authToken);
        authToken = null;
        savedGames = new GameData[0];
        return "Logged out successfully.";
    }

    //Outputs the help menu for looged-in user. Grandma should understand, grandpa might have a harder time
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
