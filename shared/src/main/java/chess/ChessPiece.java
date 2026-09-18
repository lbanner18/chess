package chess;

import java.util.*;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition startPosition) {
        List<ChessMove> moves = new ArrayList<>();
        
        switch (this.type){
            case BISHOP -> { getBishopMoves(board, startPosition, moves);}

        }
        
        return moves;

    }

    public void getBishopMoves(ChessBoard board, ChessPosition startPosition, List<ChessMove> moves) {
        int[][] directions = {
                {1, 1},
                {1, -1},
                {-1, 1},
                {-1, -1}
        };

        for (int[] dir : directions) {
            int row = startPosition.getRow();
            int col = startPosition.getColumn();

            while (true) {
                row += dir[0];
                col += dir[1];


                //Check Boundary
                if (row < 1 || row > 8 || col < 1 || col > 8) {
                    break;
                }
                ChessPosition targetPosition = new ChessPosition(row, col);
                ChessPiece pieceAtTarget = board.getPiece(targetPosition);

                //Add open square
                if (pieceAtTarget == null) {
                    moves.add(new ChessMove(startPosition, targetPosition, null));
                }

                //Check for enemy piece to add square
                else {
                    if (pieceAtTarget.getTeamColor() != this.pieceColor) {
                        moves.add(new ChessMove(startPosition, targetPosition, null));
                    }
                    break;
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }
}
