package client;

public class ClientMain {
    public static void main(String[] args) {
        var serverPort = 8080;

        new Repl(serverPort).run();
    }
}