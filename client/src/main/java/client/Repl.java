//Read, Evaluate, Print, Loop

package client;

import java.util.Scanner;
import static ui.EscapeSequences.*;

public class Repl {
    private final ChessClient client;

    public Repl(int serverPort) {
        client = new ChessClient(serverPort);
    }

    public void run() {
        System.out.println(SET_TEXT_COLOR_WHITE + "♕ Welcome to CL Chess! Type 'help' to get started. ♕");

        //For reading the user input
        Scanner scanner = new Scanner(System.in);
        var result = "";

        //This loop keeps the interaction live so long as the input isn't quit
        while (!result.equals("quit")) {
            printLogin();
            String line = scanner.nextLine();

            try {
                result = client.eval(line);
                System.out.print(SET_TEXT_COLOR_BLUE + result);
            } catch (Throwable e) {
                System.out.print(SET_TEXT_COLOR_RED + e.getMessage());
            }
        }
        System.out.println();
    }

    //An opening message for the program to show whether user's logged in or out
    private void printLogin() {
        State state = client.isLoggedIn() ? State.LOGGED_IN : State.LOGGED_OUT;
        System.out.print("\n" + SET_TEXT_COLOR_WHITE + "[" + state + "] >>> " + SET_TEXT_COLOR_GREEN);
    }

    private enum State {
        LOGGED_IN,
        LOGGED_OUT
    }
}
