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
        BISHOP,
        ROOK,
        QUEEN,
        KING,
        KNIGHT,
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
            case ROOK -> { getRookMoves(board, startPosition, moves);}
            case QUEEN -> { getQueenMoves(board, startPosition, moves);}
            case KING -> { getKingMoves(board, startPosition, moves);}
            case KNIGHT -> { getKnightMoves(board, startPosition, moves);}
            case PAWN -> { getPawnMoves(board, startPosition, moves);}
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

    public void getRookMoves(ChessBoard board, ChessPosition startPosition, List<ChessMove> moves) {
        int[][] directions = {
            {1, 0},
            {-1, 0},
            {0, 1},
            {0, -1}
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

    public void getQueenMoves(ChessBoard board, ChessPosition startPosition, List<ChessMove> moves) {
        int[][] directions = {
            {1, 0},
            {-1, 0},
            {0, 1},
            {0, -1},
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

    public void getKingMoves(ChessBoard board, ChessPosition startPosition, List<ChessMove> moves) {
        int[][] directions = {
            {1, 0},
            {-1, 0},
            {0, 1},
            {0, -1},
            {1, 1},
            {1, -1},
            {-1, 1},
            {-1, -1}
        };

        for (int[] dir : directions) {
            int row = startPosition.getRow();
            int col = startPosition.getColumn();


            row += dir[0];
            col += dir[1];


            //Check Boundary
            if (row > 0 && row < 9 && col > 0 && col < 9) {
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
                }
            }
        }
    }

    public void getKnightMoves(ChessBoard board, ChessPosition startPosition, List<ChessMove> moves) {
        int[][] directions = {
            {2, 1},
            {2, -1},
            {-2, 1},
            {-2, -1},
            {1, 2},
            {1, -2},
            {-1, 2},
            {-1, -2}
        };

        for (int[] dir : directions) {
            int row = startPosition.getRow();
            int col = startPosition.getColumn();

            row += dir[0];
            col += dir[1];

            //Check Boundary
            if (row > 0 && row < 9 && col > 0 && col < 9) {
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
                }
            }
        }
    }

    public void pawnPromotionMoves(ChessBoard board, ChessPosition startPosition, ChessPosition targetPosition, List<ChessMove> moves) {
        moves.add(new ChessMove(startPosition, targetPosition, PieceType.KNIGHT));
        moves.add(new ChessMove(startPosition, targetPosition, PieceType.BISHOP));
        moves.add(new ChessMove(startPosition, targetPosition, PieceType.ROOK));
        moves.add(new ChessMove(startPosition, targetPosition, PieceType.QUEEN));
    }

    public void getPawnMoves(ChessBoard board, ChessPosition startPosition, List<ChessMove> moves) {
        int row = startPosition.getRow();
        int col = startPosition.getColumn();

        if (this.getTeamColor() == ChessGame.TeamColor.WHITE) {

            //Standard Forward Move
            ChessPosition targetPosition = new ChessPosition(row + 1, col);
            ChessPiece targetPiece = board.getPiece(targetPosition);
            if (targetPiece == null) {
                if (row == 7) {
                    pawnPromotionMoves(board, startPosition, targetPosition, moves);
                }
                else {
                    moves.add(new ChessMove(startPosition, targetPosition, null));
                }
            }

            //Check for first move exception
            if (row == 2) {
                ChessPosition firstMoveTargetPosition = new ChessPosition(row + 2, col);
                ChessPiece firstMoveTargetPiece = board.getPiece(firstMoveTargetPosition);
                if (targetPiece == null && firstMoveTargetPiece == null) {
                    moves.add(new ChessMove(startPosition, firstMoveTargetPosition, null));
                }
            }

            //Check for enemy available to capture in the front-left
            if (col > 1) {
                ChessPosition enemyLeft = new ChessPosition(row + 1, col - 1);
                ChessPiece enemyLeftPiece = board.getPiece(enemyLeft);
                if (enemyLeftPiece != null && enemyLeftPiece.getTeamColor() == ChessGame.TeamColor.BLACK && row < 7) {
                    moves.add(new ChessMove(startPosition, enemyLeft, null));
                }
                if (enemyLeftPiece != null && enemyLeftPiece.getTeamColor() == ChessGame.TeamColor.BLACK && row == 7) {
                    pawnPromotionMoves(board, startPosition, enemyLeft, moves);
                }
            }

            //Check for enemy available to capture in the front-right
            if (col < 8) {
                ChessPosition enemyRight = new ChessPosition(row + 1, col + 1);
                ChessPiece enemyRightPiece = board.getPiece(enemyRight);
                if (enemyRightPiece != null && enemyRightPiece.getTeamColor() == ChessGame.TeamColor.BLACK && row < 7) {
                    moves.add(new ChessMove(startPosition, enemyRight, null));
                }
                if (enemyRightPiece != null && enemyRightPiece.getTeamColor() == ChessGame.TeamColor.BLACK && row == 7) {
                    pawnPromotionMoves(board, startPosition, enemyRight, moves);
                }
            }
        }

        else {

            //Standard Forward Move
            ChessPosition targetPosition = new ChessPosition(row - 1, col);
            ChessPiece targetPiece = board.getPiece(targetPosition);
            if (targetPiece == null) {
                if (row == 2) {
                    pawnPromotionMoves(board, startPosition, targetPosition, moves);
                }
                else {
                    moves.add(new ChessMove(startPosition, targetPosition, null));
                }
            }

            //Check for first move exception
            if (row == 7) {
                ChessPosition firstMoveTargetPosition = new ChessPosition(row - 2, col);
                ChessPiece firstMoveTargetPiece = board.getPiece(firstMoveTargetPosition);
                if (targetPiece == null && firstMoveTargetPiece == null) {
                    moves.add(new ChessMove(startPosition, firstMoveTargetPosition, null));
                }
            }

            //Check for enemy available to capture in the front-left
            if (col > 1) {
                ChessPosition enemyLeft = new ChessPosition(row - 1, col - 1);
                ChessPiece enemyLeftPiece = board.getPiece(enemyLeft);
                if (enemyLeftPiece != null && enemyLeftPiece.getTeamColor() == ChessGame.TeamColor.WHITE && row > 2) {
                    moves.add(new ChessMove(startPosition, enemyLeft, null));
                }
                if (enemyLeftPiece != null && enemyLeftPiece.getTeamColor() == ChessGame.TeamColor.WHITE && row == 2) {
                    pawnPromotionMoves(board, startPosition, enemyLeft, moves);
                }
            }

            //Check for enemy available to capture in the front-right
            if (col < 8) {
                ChessPosition enemyRight = new ChessPosition(row - 1, col + 1);
                ChessPiece enemyRightPiece = board.getPiece(enemyRight);
                if (enemyRightPiece != null && enemyRightPiece.getTeamColor() == ChessGame.TeamColor.WHITE && row > 2) {
                    moves.add(new ChessMove(startPosition, enemyRight, null));
                }
                if (enemyRightPiece != null && enemyRightPiece.getTeamColor() == ChessGame.TeamColor.WHITE && row == 2) {
                    pawnPromotionMoves(board, startPosition, enemyRight, moves);
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
