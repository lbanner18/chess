package server;

import com.google.gson.Gson;
import model.RegisterRequest;
import model.RegisterResult;
import model.LoginRequest;
import model.LoginResult;
import model.CreateGameRequest;
import model.CreateGameResult;
import model.JoinGameRequest;
import model.ListGamesResult;
import service.UserService;
import service.GameService;
import dataaccess.DataAccessException;
import io.javalin.Javalin;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import service.ClearService;

public class Server {
    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        MemoryUserDAO userDAO = new MemoryUserDAO();
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        MemoryGameDAO gameDAO = new MemoryGameDAO();

        ClearService clearService = new ClearService(userDAO, authDAO, gameDAO);
        UserService userService = new UserService(userDAO, authDAO);
        GameService gameService = new GameService(gameDAO, authDAO);

        setupUserRoutes(userService);
        setupGameRoutes(gameService);
        setupClearRoutes(clearService);
    }

    private void setupClearRoutes(ClearService clearService) {
        javalin.delete("/db", ctx -> {
            try {
                clearService.clear();
                ctx.status(200);
                ctx.result("{}");
            } catch (Exception e) {
                ctx.status(500);
                ctx.result("{ \"message\": \"Error: " + e.getMessage() + "\" }");
            }
        });
    }

    private void setupUserRoutes(UserService userService) {
        Gson gson = new Gson();

        //Register Endpoint
        javalin.post("/user", ctx -> {
            try {
                RegisterRequest request = gson.fromJson(ctx.body(), RegisterRequest.class);

                RegisterResult result = userService.register(request);
                ctx.status(200);
                ctx.result(gson.toJson(result));
            } catch (DataAccessException e) {
                if (e.getMessage().equals("Error: bad request")) {
                    ctx.status(400);
                } else if (e.getMessage().equals("Error: already taken")) {
                    ctx.status(403);
                } else {
                    ctx.status(500);
                }
                ctx.result("{ \"message\": \"" + e.getMessage() + "\" }");
            }
        });

        // Login Endpoint
        javalin.post("/session", ctx -> {
            try {
                LoginRequest request = gson.fromJson(ctx.body(), LoginRequest.class);
                LoginResult result = userService.login(request);

                ctx.status(200);
                ctx.result(gson.toJson(result));
            } catch (DataAccessException e) {
                if (e.getMessage().equals("Error: unauthorized")) {
                    ctx.status(401);
                } else if (e.getMessage().equals("Error: bad request")) {
                    ctx.status(400);
                } else {
                    ctx.status(500);
                }
                ctx.result("{ \"message\": \"" + e.getMessage() + "\" }");
            }
        });

        // Logout Endpoint
        javalin.delete("/session", ctx -> {
            try {
                String authToken = ctx.header("authorization");

                userService.logout(authToken);

                ctx.status(200);
                ctx.result("{}");
            } catch (DataAccessException e) {
                if (e.getMessage().equals("Error: unauthorized")) {
                    ctx.status(401);
                } else {
                    ctx.status(500);
                }
                ctx.result("{ \"message\": \"" + e.getMessage() + "\" }");
            }
        });
    }


    private void setupGameRoutes(GameService gameService) {
        Gson gson = new Gson();

        // List Games Endpoint
        javalin.get("/game", ctx -> {
            try {
                String authToken = ctx.header("authorization");
                ListGamesResult result = gameService.listGames(authToken);

                ctx.status(200);
                ctx.result(gson.toJson(result));
            } catch (DataAccessException e) {
                ctx.status(401);
                ctx.result("{ \"message\": \"" + e.getMessage() + "\" }");
            }
        });

        // Create Game Endpoint
        javalin.post("/game", ctx -> {
            try {
                String authToken = ctx.header("authorization");
                CreateGameRequest request = gson.fromJson(ctx.body(), CreateGameRequest.class);
                CreateGameResult result = gameService.createGame(authToken, request);

                ctx.status(200);
                ctx.result(gson.toJson(result));
            } catch (DataAccessException e) {
                if (e.getMessage().equals("Error: bad request")) {
                    ctx.status(400);
                } else {
                    ctx.status(401);
                }
                ctx.result("{ \"message\": \"" + e.getMessage() + "\" }");
            }
        });

        // Join Game Endpoint (Notice this is a PUT request, not POST)
        javalin.put("/game", ctx -> {
            try {
                String authToken = ctx.header("authorization");
                JoinGameRequest request = gson.fromJson(ctx.body(), JoinGameRequest.class);

                gameService.joinGame(authToken, request);

                ctx.status(200);
                ctx.result("{}");
            } catch (DataAccessException e) {
                if (e.getMessage().equals("Error: bad request")) {
                    ctx.status(400);
                } else if (e.getMessage().equals("Error: unauthorized")) {
                    ctx.status(401);
                } else if (e.getMessage().equals("Error: already taken")) {
                    ctx.status(403);
                } else {
                    ctx.status(500);
                }
                ctx.result("{ \"message\": \"" + e.getMessage() + "\" }");
            }
        });
    }




    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}