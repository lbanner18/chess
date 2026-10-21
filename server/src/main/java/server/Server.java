package server;

import com.google.gson.Gson;
import model.RegisterRequest;
import model.RegisterResult;
import model.LoginRequest;
import model.LoginResult;
import service.UserService;
import dataaccess.DataAccessException;
import io.javalin.Javalin;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import service.ClearService;

public class Server {
    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create();

        MemoryUserDAO userDAO = new MemoryUserDAO();
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        MemoryGameDAO gameDAO = new MemoryGameDAO();

        ClearService clearService = new ClearService(userDAO, authDAO, gameDAO);
        UserService userService = new UserService(userDAO, authDAO); // <-- NEW

        Gson gson = new Gson(); // <-- NEW

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

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}