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
import dataaccess.SqlAuthDAO;
import dataaccess.SqlGameDAO;
import dataaccess.SqlUserDAO;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import service.ClearService;

public class Server {
    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        UserDAO userDAO;
        AuthDAO authDAO;
        GameDAO gameDAO;

        try {
            userDAO = new SqlUserDAO();
            authDAO = new SqlAuthDAO();
            gameDAO = new SqlGameDAO();
        } catch (DataAccessException e) {
            System.err.println("Unable to start server because of database error: " + e.getMessage());
            throw new RuntimeException(e);
        }

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
                ctx.result("{ \"message\": \"Error: " + e.getMessage().replace("Error: ", "") + "\" }");
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
                ctx.result("{ \"message\": \"Error: " + e.getMessage().replace("Error: ", "") + "\" }");
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
                ctx.result("{ \"message\": \"Error: " + e.getMessage().replace("Error: ", "") + "\" }");
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
                ctx.result("{ \"message\": \"Error: " + e.getMessage().replace("Error: ", "") + "\" }");
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
                if (e.getMessage().equals("Error: unauthorized")) {
                    ctx.status(401);
                } else {
                    ctx.status(500);
                }
                ctx.result("{ \"message\": \"Error: " + e.getMessage().replace("Error: ", "") + "\" }");
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
                } else if (e.getMessage().equals("Error: unauthorized")) {
                    ctx.status(401);
                } else {
                    ctx.status(500);
                }
                ctx.result("{ \"message\": \"Error: " + e.getMessage().replace("Error: ", "") + "\" }");
            }
        });

        // Join Game Endpoint
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
                ctx.result("{ \"message\": \"Error: " + e.getMessage().replace("Error: ", "") + "\" }");
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