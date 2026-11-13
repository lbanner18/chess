package client;

import com.google.gson.Gson;
import model.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;

public class ServerFacade {

    private final String serverUrl;
    private final Gson gson = new Gson();

    public ServerFacade(int port) {
        this.serverUrl = "http://localhost:" + port;
    }

    public RegisterResult register(RegisterRequest request) throws ResponseException {
        return makeRequest("POST", "/user", request, RegisterResult.class, null);
    }

    public LoginResult login(LoginRequest request) throws ResponseException {
        return makeRequest("POST", "/session", request, LoginResult.class, null);
    }

    public void logout(String authToken) throws ResponseException {
        makeRequest("DELETE", "/session", null, null, authToken);
    }

    public CreateGameResult createGame(CreateGameRequest request, String authToken) throws ResponseException {
        return makeRequest("POST", "/game", request, CreateGameResult.class, authToken);
    }

    public ListGamesResult listGames(String authToken) throws ResponseException {
        return makeRequest("GET", "/game", null, ListGamesResult.class, authToken);
    }

    public void joinGame(JoinGameRequest request, String authToken) throws ResponseException {
        makeRequest("PUT", "/game", request, null, authToken);
    }

    public void clear() throws ResponseException {
        makeRequest("DELETE", "/db", null, null, null);
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String authToken) throws ResponseException {
        try {
            URL chessURL = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) chessURL.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            if (authToken != null) {
                http.addRequestProperty("authorization", authToken);
            }

            if (request != null) {
                http.addRequestProperty("Content-Type", "application/json");
                try (OutputStream reqBody = http.getOutputStream()) {
                    String jsonRequest = gson.toJson(request);
                    reqBody.write(jsonRequest.getBytes());
                }
            }

            http.connect();

            if (http.getResponseCode() >= 400) {
                throw new ResponseException(http.getResponseCode(), extractErrorMessage(http.getErrorStream()));
            }

            if (responseClass != null) {
                try (InputStream respBody = http.getInputStream()) {
                    InputStreamReader reader = new InputStreamReader(respBody);
                    return gson.fromJson(reader, responseClass);
                }
            }

            return null;

        } catch (ResponseException error) {
            throw error;
        } catch (Exception error) {
            throw new ResponseException(500, "Connection to server failed: " + error.getMessage());
        }
    }

    private String extractErrorMessage(InputStream errorStream) {
        try {
            if (errorStream == null) return "Unknown error";
            InputStreamReader reader = new InputStreamReader(errorStream);
            java.util.Map<String, String> errorMap = gson.fromJson(reader, java.util.Map.class);
            return errorMap.getOrDefault("message", "Unknown error");
        } catch (Exception error) {
            return "Failed to read error response";
        }
    }
}