package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static ui.EscapeSequences.*;

public class BoardPrinter {
    private static final int BOARD_SIZE = 8;
    /*
    *My board was being incredibly wonky with the spacing. So after reading the troubleshooting tips,
    *I found full-width unicode characters that I could paste into my code which matched the sizing of the pieces
    */
    private static final String[] LETTERS = {" ａ ", " ｂ ", " ｃ ", " ｄ ", " ｅ ", " ｆ ", " ｇ ", " ｈ "};

    //whitePlayer is a bool that determines perspective (is it of White?)
    //Clear and print board
    public static void printBoard(ChessBoard board, boolean whitePlayer) {
        //Helps the board play nice with the terminal
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);

        out.print(ERASE_SCREEN);
        out.println();

        drawHeaders(out, whitePlayer);
        drawChessBoard(out, board, whitePlayer);
        drawHeaders(out, whitePlayer);

        out.print(RESET_BG_COLOR);
        out.print(SET_TEXT_COLOR_WHITE);
        out.println();
    }

    //Draw A-H and 1-8
    private static void drawHeaders(PrintStream out, boolean whitePlayer) {
        out.print(SET_BG_COLOR_LIGHT_GREY);
        out.print(SET_TEXT_COLOR_BLACK);
        out.print("   ");

        //Determines order of output letters
        for (int i = 0; i < BOARD_SIZE; i++) {
            int col = whitePlayer ? i : 7 - i;
            out.print(LETTERS[col]);
        }

        out.print("   ");
        out.print(RESET_BG_COLOR);
        out.println();
    }

    //Grabs the board state from ChessBoard and draws it utilizing its methods
    private static void drawChessBoard(PrintStream out, ChessBoard board, boolean whitePlayer) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            //Prints the rows
            int row = whitePlayer ? 8 - i : i + 1;

            out.print(SET_BG_COLOR_LIGHT_GREY);
            out.print(SET_TEXT_COLOR_BLACK);
            out.print(" " + row + " ");

            for (int j = 0; j < BOARD_SIZE; j++) {
                //prints the squares. isLightSquare determines which squares are which color
                int col = whitePlayer ? j + 1 : 8 - j;
                boolean isLightSquare = (row + col) % 2 != 0;
                out.print(isLightSquare ? SET_BG_COLOR_WHITE : SET_BG_COLOR_BLACK);

                ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                //Calls helper function
                printPiece(out, piece);
            }

            out.print(SET_BG_COLOR_LIGHT_GREY);
            out.print(SET_TEXT_COLOR_BLACK);
            out.print(" " + row + " ");
            out.print(RESET_BG_COLOR);
            out.println();
        }
    }

    //Helper function to determine piece color and to print it
    private static void printPiece(PrintStream out, ChessPiece piece) {
        if (piece == null) {
            out.print(EMPTY);
        } else {
            out.print(piece.getTeamColor() == ChessGame.TeamColor.WHITE ? SET_TEXT_COLOR_RED : SET_TEXT_COLOR_BLUE);
            out.print(getPieceSymbol(piece));
        }
    }

    //Helper function to determine piece type
    private static String getPieceSymbol(ChessPiece piece) {
        return switch (piece.getPieceType()) {
            case KING -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_KING : BLACK_KING;
            case QUEEN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_QUEEN : BLACK_QUEEN;
            case BISHOP -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_BISHOP : BLACK_BISHOP;
            case KNIGHT -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_KNIGHT : BLACK_KNIGHT;
            case ROOK -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_ROOK : BLACK_ROOK;
            case PAWN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_PAWN : BLACK_PAWN;
        };
    }
}