package tablut;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/** A Player that automatically generates moves.
 *  @author Razi Mahmood
 */
class AI extends Player {

    /** A position-score magnitude indicating a win (for white if positive,
     *  black if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /** A position-score magnitude indicating a forced win in a subsequent
     *  move.  This differs from WINNING_VALUE to avoid putting off wins. */
    private static final int WILL_WIN_VALUE = Integer.MAX_VALUE - 40;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new AI with no piece or controller (intended to produce
     *  a template). */
    AI() {
        this(null, null);
    }

    /** A new AI playing PIECE under control of CONTROLLER. */
    AI(Piece piece, Controller controller) {
        super(piece, controller);
    }

    @Override
    Player create(Piece piece, Controller controller) {
        return new AI(piece, controller);
    }

    @Override
    String myMove() {

        if (board().winner() != null || board().turn() != myPiece()) {
            _controller.reportError("I lost");
        } else {
            Move move = findMove();
            if (move == null || !(board().isLegal(move))) {
                _controller.reportError("I lost");
            } else {
                System.out.println(myPiece() + " Move : " + move.toString());
                return move.toString();
            }
        }
        return null;
    }
    /** Meant to iterate over the board's moves.
     * @param b a board example
     * @param allmoves a list of moves
     * @return a move
     * */
    Move getBestMove(Board b, List<Move> allmoves) {
        if (allmoves == null) {
            return null;
        }
        Iterator it = allmoves.iterator();
        Move m;
        return (Move) it.next();
    }

    @Override
    boolean isManual() {
        return false;
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = new Board(board());
        _lastFoundMove = null;

        findMove(b, maxDepth(b), true, 1, -INFTY, INFTY);
        return _lastFoundMove;
    }

    /** The move found by the last call to one of the ...FindMove methods
     *  below. */
    private Move _lastFoundMove;


    /** Find a move from position BOARD and return its value, recording
     *  the move found in _lastFoundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _lastMoveFound. */
    private int findMove(Board board, int depth, boolean saveMove,
                         int sense, int alpha, int beta) {
        int value;
        Piece p;
        if (depth == 0 || board.winner() != null) {
            return staticScore(board);
        }

        if (sense == 1) {
            double bestvalue = -INFTY;
            for (Move m : board.legalMoves(myPiece())) {
                p = board.opponentPiece(myPiece());
                board.setPreviousConfiguration(board, p);
                System.out.println("Trying move for " + myPiece() + " = " + m);
                board.makeMove(m);
                value = findMove(board, depth - 1, false, -1, alpha, beta);
                if (value > bestvalue) {
                    bestvalue = Math.max(bestvalue, value);
                    if (saveMove) {
                        _lastFoundMove = m;
                    }
                }
                alpha = (int) Math.max(alpha, bestvalue);

                if (beta <= alpha) {
                    break;
                }
                board.undo();
            }
            return (int) bestvalue;
        } else {
            int bestvalue = INFTY;
            for (Move m : board.legalMoves(board.opponentPiece(myPiece()))) {
                board.setPreviousConfiguration(board, myPiece());
                board.makeMove(m);
                value = findMove(board, depth - 1, false, 1, alpha, beta);
                bestvalue = Math.min(bestvalue, value);
                beta = Math.min(beta, bestvalue);
                if (beta <= alpha) {
                    break;
                }
                board.undo();
            }
            return bestvalue;
        }
    }


    /** Return a heuristically determined maximum search depth
     *  based on characteristics of BOARD. */
    private static int maxDepth(Board board) {
        return 1;
    }

    /** Return a heuristic value for BOARD. */
    private int staticScore(Board board) {

        if (board.turn() == Piece.WHITE) {
            return getWhiteScore(board);
        } else {
            return getBlackScore(board);
        }

    }
    /** Meant to calculate the black heuristic score.
     * @param board a board example
     * @return blackpiecescore
     * */
    int getBlackScore(Board board) {

        HashSet<Square> blackPieces = board.pieceLocations(Piece.BLACK);
        double blackpiecescore = (double) blackPieces.size() / 16.0;
        double score = blackpiecescore;
        if (board.kingPosition() == null) {
            score += 1.0;
        }
        return (int) score;
    }
    /** Meant to calculate the white heuristic score.
     * @param board a board example
     * @return whitepiecescore
     * */
    int getWhiteScore(Board board) {
        Square kinpos = board.kingPosition();
        if (kinpos == null) {
            return 0;
        }
        double distx = Math.min(board.SIZE - kinpos.col(), kinpos.col());
        double disty = Math.min(board.SIZE - kinpos.row(), kinpos.row());
        double mindist = Math.min(distx, disty);
        mindist = mindist / board.SIZE;

        mindist = 1 - mindist;
        HashSet<Square> whitePieces = board.pieceLocations(Piece.WHITE);
        double whitepiecescore = (double) whitePieces.size() / 8.0;

        double score = whitepiecescore + mindist;
        return (int) score;
    }
}
