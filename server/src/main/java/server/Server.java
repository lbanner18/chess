package server;

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

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
