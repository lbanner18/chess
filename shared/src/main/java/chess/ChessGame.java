package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * A class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor teamTurn;
    private ChessBoard board;

    private boolean whiteKingMoved = false;
    private boolean whiteKingsideRookMoved = false;
    private boolean whiteQueensideRookMoved = false;

    private boolean blackKingMoved = false;
    private boolean blackKingsideRookMoved = false;
    private boolean blackQueensideRookMoved = false;

    private ChessPosition enPassantTarget = null;

   public ChessGame() {
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.teamTurn = TeamColor.WHITE;
        resetCastlingRights();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Sets which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets all valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }
        Collection<ChessMove> possibleMoves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();
        for (ChessMove move : possibleMoves) {

            // Simulate move
            ChessPiece capturedPiece = board.getPiece(move.getEndPosition());
            board.addPiece(move.getEndPosition(), piece);
            board.addPiece(move.getStartPosition(), null);

            // Check for checks (check check)
            if (!isInCheck(piece.getTeamColor())) {
                validMoves.add(move);
            }

            // Revert board
            board.addPiece(move.getStartPosition(), piece);
            board.addPiece(move.getEndPosition(), capturedPiece);

        }

        //check for castling
        if (piece.getPieceType() == ChessPiece.PieceType.KING && !isInCheck(piece.getTeamColor())) {
            int row = (piece.getTeamColor() == TeamColor.WHITE) ? 1 : 8;
            boolean kingMoved = (piece.getTeamColor() == TeamColor.WHITE) ? whiteKingMoved : blackKingMoved;
            boolean kingsideRookMoved = (piece.getTeamColor() == TeamColor.WHITE) ? whiteKingsideRookMoved : blackKingsideRookMoved;
            boolean queensideRookMoved = (piece.getTeamColor() == TeamColor.WHITE) ? whiteQueensideRookMoved : blackQueensideRookMoved;

            ChessPiece kingsideRook = board.getPiece(new ChessPosition(row, 8));
            boolean hasKingsideRook = kingsideRook != null
                    && kingsideRook.getPieceType() == ChessPiece.PieceType.ROOK
                    && kingsideRook.getTeamColor() == piece.getTeamColor();

            if (!kingMoved && startPosition.getRow() == row && startPosition.getColumn() == 5) {
                if (!kingsideRookMoved
                        && hasKingsideRook
                        && board.getPiece(new ChessPosition(row, 6)) == null
                        && board.getPiece(new ChessPosition(row, 7)) == null
                        && !squareUnderAttack(new ChessPosition(row, 6), piece.getTeamColor())
                        && !squareUnderAttack(new ChessPosition(row, 7), piece.getTeamColor())) {
                    validMoves.add(new ChessMove (startPosition, new ChessPosition(row, 7), null ));
                }

                ChessPiece queensideRook = board.getPiece(new ChessPosition(row, 1));
                boolean hasQueensideRook = queensideRook != null
                        && queensideRook.getPieceType() == ChessPiece.PieceType.ROOK
                        && queensideRook.getTeamColor() == piece.getTeamColor();

                if (!queensideRookMoved
                        && hasQueensideRook
                        && board.getPiece(new ChessPosition(row, 2)) == null
                        && board.getPiece(new ChessPosition(row, 3)) == null
                        && board.getPiece(new ChessPosition(row, 4)) == null
                        && !squareUnderAttack(new ChessPosition(row, 3), piece.getTeamColor())
                        && !squareUnderAttack(new ChessPosition(row, 4), piece.getTeamColor())) {
                    validMoves.add(new ChessMove (startPosition, new ChessPosition(row, 3), null ));
                }
            }
        }

        //check for en passant
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN && enPassantTarget != null) {
            int startRow = startPosition.getRow();
            int startCol = startPosition.getColumn();
            int targetRow = enPassantTarget.getRow();
            int targetCol = enPassantTarget.getColumn();

            boolean isWhiteEligible = (piece.getTeamColor() == TeamColor.WHITE && startRow == 5 && targetRow == 6);
            boolean isBlackEligible = (piece.getTeamColor() == TeamColor.BLACK && startRow == 4 && targetRow == 3);

            if ((isWhiteEligible || isBlackEligible) && (startCol - targetCol == 1 || startCol - targetCol == -1)) {
                ChessPosition capturedPawn = new ChessPosition(startRow, targetCol);
                ChessPiece enemyPawn = board.getPiece(capturedPawn);

                board.addPiece(enPassantTarget, piece);
                board.addPiece(startPosition, null);
                board.addPiece(capturedPawn, null);

                if (!isInCheck(piece.getTeamColor())){
                    validMoves.add(new ChessMove(startPosition, enPassantTarget, null));
                }

                board.addPiece(startPosition, piece);
                board.addPiece(enPassantTarget, null);
                board.addPiece(capturedPawn, enemyPawn);
            }
        }

        return validMoves;
    }

    /**
     * Makes a move in the chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());

        //Move legality
        if (piece == null) {
            throw new InvalidMoveException("No piece at position");
        }
        if (piece.getTeamColor() != getTeamTurn()) {
            throw new InvalidMoveException("Not this piece's team's turn");
        }
        Collection<ChessMove> legalMoves = validMoves(move.getStartPosition());
        if (legalMoves == null || !legalMoves.contains(move)) {
            throw new InvalidMoveException("Move is not legal");
        }

        //Promotion pieces
        if (move.getPromotionPiece() != null) {
            board.addPiece(move.getEndPosition(), new ChessPiece(piece.getTeamColor(), move.getPromotionPiece()));
        } else {
            board.addPiece(move.getEndPosition(), piece);
        }
        board.addPiece(move.getStartPosition(), null);

        //Castling move
        if (piece.getPieceType() == ChessPiece.PieceType.KING && move.getStartPosition().getColumn() == 5) {
            int row = move.getStartPosition().getRow();

            if (move.getEndPosition().getColumn() == 7) {
                board.addPiece(new ChessPosition(row, 6), board.getPiece(new ChessPosition(row, 8)));
                board.addPiece(new ChessPosition(row, 8), null);
            }

            if (move.getEndPosition().getColumn() == 3) {
                board.addPiece(new ChessPosition(row, 4), board.getPiece(new ChessPosition(row, 1)));
                board.addPiece(new ChessPosition(row, 1), null);
            }

        }

        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();

        if (start.equals(new ChessPosition(1, 5))) whiteKingMoved = true;
        if (start.equals(new ChessPosition(1, 8)) || end.equals(new ChessPosition(1, 8))) whiteKingsideRookMoved = true;
        if (start.equals(new ChessPosition(1, 1)) || end.equals(new ChessPosition(1, 1))) whiteQueensideRookMoved = true;

        if (start.equals(new ChessPosition(8, 5))) blackKingMoved = true;
        if (start.equals(new ChessPosition(8, 8)) || end.equals(new ChessPosition(8, 8))) blackKingsideRookMoved = true;
        if (start.equals(new ChessPosition(8, 1)) || end.equals(new ChessPosition(8, 1))) blackQueensideRookMoved = true;

        //Make en passant move
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN && move.getEndPosition().equals(enPassantTarget)) {
            int targetRow = move.getStartPosition().getRow();
            int targetCol = move.getEndPosition().getColumn();
            board.addPiece(new ChessPosition(targetRow, targetCol), null);
        }

        //check for En Passant
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN ) {
            int startRow = move.getStartPosition().getRow();
            int endRow = move.getEndPosition().getRow();
            int col = move.getStartPosition().getColumn();

            if (startRow == 2 && endRow == 4) {
                enPassantTarget = new ChessPosition(3, col);
            } else if (startRow == 7 && endRow == 5) {
                enPassantTarget = new ChessPosition(6, col);
            } else {
                enPassantTarget = null;
            }
        }
        else {
            enPassantTarget = null;
        }

        //Toggle Turn
        setTeamTurn((teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE);
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */

    private ChessPosition findKing(TeamColor teamColor) {
        for (int row = 1; row < 9; row++) {
            for (int col = 1; col < 9; col++) {
                ChessPosition kingSquare = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(kingSquare);

                if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == teamColor) {
                    return kingSquare;
                }
            }
        }
        return null;
    }

    public boolean squareUnderAttack(ChessPosition position, TeamColor teamColor) {
        if (position == null) {
            return false;
        }
            for (int row = 1; row < 9; row++) {
            for (int col = 1; col < 9; col++) {
                ChessPosition enemySquare = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(enemySquare);

                if (piece != null && piece.getTeamColor() != teamColor) {
                    Collection<ChessMove> enemyMoves = piece.pieceMoves(board, enemySquare);
                    for (ChessMove move : enemyMoves) {
                        if (move.getEndPosition().equals(position)) {
                            return true;
                        }
                    }
                }
            }
        }
            return false;
    }

    public boolean noValidMoves(TeamColor teamColor) {
        for (int row = 1; row < 9; row++) {
            for (int col = 1; col < 9; col++) {
                ChessPosition pieceSquare = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pieceSquare);

                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = validMoves(pieceSquare);
                    if (moves != null && !moves.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingSquare = findKing(teamColor);
        return squareUnderAttack(kingSquare, teamColor);
    }


    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && noValidMoves(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && noValidMoves(teamColor);
    }

    /**
     * Sets this game's chessboard to a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
        resetCastlingRights();
        enPassantTarget = null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return teamTurn == chessGame.teamTurn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamTurn, board);
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }

    private void resetCastlingRights() {
        whiteKingMoved = false;
        whiteKingsideRookMoved = false;
        whiteQueensideRookMoved = false;

        blackKingMoved = false;
        blackKingsideRookMoved = false;
        blackQueensideRookMoved = false;
    }
}
