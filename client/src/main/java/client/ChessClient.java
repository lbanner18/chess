package client;

import java.util.Arrays;
import model.RegisterRequest;
import model.LoginRequest;

public class ChessClient {
    private final ServerFacade server;
    private String authToken = null;

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

    //Pre-Login

    private String register(String[] params) throws ResponseException {
        if (params.length == 3) {
            var request = new RegisterRequest(params[0], params[1], params[2]);
            var result = server.register(request);
            authToken = result.authToken();
            return String.format("Successfully registered and logged in as %s.", result.username());
        }
        return "Expected syntax: register <username> <password> <email>";
    }

    private String login(String[] params) throws ResponseException {
        if (params.length == 2) {
            var request = new LoginRequest(params[0], params[1]);
            var result = server.login(request);
            authToken = result.authToken();
            return String.format("Successfully logged in as %s.", result.username());
        }
        return "Expected syntax: login <username> <password>";
    }

    private String helpPreLogin() {
        return """
                - register <username> <password> <email> - Create an account
                 - login <username> <password> - Login to play and observe Chess
                 - quit - Exit CL Chess
                 - help - List available commands
                 """;
    }
}
