package tablut;

import static tablut.Piece.BLACK;
import static tablut.Piece.EMPTY;
import static tablut.Piece.KING;
import static tablut.Piece.WHITE;
import static tablut.Square.SQUARE_LIST;
import static tablut.Square.sq;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;


/** The state of a Tablut Game.
 *  @author Razi Mahmood
 */
class Board {

    /** The number of squares on a side of the board. */
    static final int SIZE = 9;

    /** The throne (or castle) square and its four surrounding squares. */
    static final Square THRONE = sq(4, 4),
            NTHRONE = sq(4, 5),
            STHRONE = sq(4, 3),
            WTHRONE = sq(3, 4),
            ETHRONE = sq(5, 4);

    /** Initial positions of attackers. */
    static final Square[] INITIAL_ATTACKERS = {
            sq(0, 3), sq(0, 4), sq(0, 5), sq(1, 4),
            sq(8, 3), sq(8, 4), sq(8, 5), sq(7, 4),
            sq(3, 0), sq(4, 0), sq(5, 0), sq(4, 1),
            sq(3, 8), sq(4, 8), sq(5, 8), sq(4, 7)
    };

    /** Initial positions of defenders of the king. */
    static final Square[] INITIAL_DEFENDERS =
            { NTHRONE, ETHRONE, STHRONE, WTHRONE,
              sq(4, 6), sq(4, 2), sq(2, 4), sq(6, 4) };

    /** Initializes a game board with SIZE squares on a side in the
     *  initial position. */
    Board() {
        init();
    }

    /** Initializes a copy of MODEL. */
    Board(Board model) {
        copy(model);
    }

    /** Copies MODEL into me. */
    void copy(Board model) {
        if (model == this) {
            return;
        }
        init();

        this._turn = model._turn;
        this._winner = model._winner;
        this._moveCount = model._moveCount;
        this.n_limit = model.n_limit;

        Piece p;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                p = model._myboardelements[i][j];
                this._myboardelements[i][j] = p;
            }
        }
        if (model._previousStates != null) {
            Iterator it = model._previousStates.keySet().iterator();
            String key;
            BoardState bs;
            this._previousStates = new HashMap<>();
            while (it.hasNext()) {
                key = (String) it.next();
                bs = model._previousStates.get(key);
                this._previousStates.put(key, bs);
            }
        }
        this.prevConfig = null;
    }

    /** Clears the board to the initial position. */
    void init() {

        _myboardelements = new Piece[SIZE][SIZE];
        for (int i = 0; i < _myboardelements.length; i++) {
            for (int j = 0; j < SIZE; j++) {
                _myboardelements[i][j] = EMPTY;
            }
        }
        for (Square sq: INITIAL_ATTACKERS) {
            _myboardelements[sq.col()][sq.row()] = BLACK;
        }
        for (Square sq: INITIAL_DEFENDERS) {
            _myboardelements[sq.col()][sq.row()] = WHITE;
        }
        _myboardelements[THRONE.col()][THRONE.row()] = KING;
        _turn = BLACK;
        _moveCount = 0;
        n_limit = 50;

    }

    /** Set the move limit to LIM.  It is an error if 2*LIM <= moveCount().
     * @param n a limit
     * */
    void setMoveLimit(int n) {
        n_limit = 2 * n;
    }

    /** Return a Piece representing whose move it is (WHITE or BLACK). */
    Piece turn() {
        return _turn;
    }

    /** Return the winner in the current position, or null if there is no winner
     *  yet. */
    Piece winner() {
        return _winner;
    }

    /** Returns true iff this is a win due to a repeated position. */
    boolean repeatedPosition() {
        return _repeated;
    }

    /** Record current position and set winner() next mover if the current
     *  position is a repeat. */
    private ArrayList<String> encodedStrings = new ArrayList<String>();

    /** Meant to check if the board repeats.*/
    private void checkRepeated() {
        String encodecheck = encodedBoard();
        if (!(encodedStrings.contains(encodecheck))) {
            _repeated = false;
            encodedStrings.add(encodedBoard());
            _winner = _turn.opponent();
        } else {
            _repeated = true;
        }
    }

    /** Return the number of moves since the initial position that have not been
     *  undone. */
    int moveCount() {
        return _moveCount;
    }

    /** Return location of the king. */
    Square kingPosition() {
        for (int i = 0; i < _myboardelements.length; i++) {
            for (int j = 0; j < _myboardelements[0].length; j++) {
                if (_myboardelements[i][j] == KING) {
                    return sq(i, j);
                }
            }
        }
        return null;
    }

    /** Return the contents the square at S. */
    final Piece get(Square s) {
        return get(s.col(), s.row());
    }

    /** Return the contents of the square at (COL, ROW), where
     *  0 <= COL, ROW <= 9. */
    final Piece get(int col, int row) {
        return _myboardelements[col][row];
    }

    /** Return the contents of the square at COL ROW. */
    final Piece get(char col, char row) {
        return get(row - '1', col - 'a');
    }

    /** Set square S to P. */
    final void put(Piece p, Square s) {

        int x = s.col();
        int y = s.row();

        _myboardelements[x][y] = p;
    }

    /** Set square S to P and record for undoing. */
    final void revPut(Piece p, Square s) {
        if (_pieceStack == null) {
            _pieceStack = new Stack<>();
        }
        if (_squareStack == null) {
            _squareStack = new Stack<>();
        }
        _pieceStack.push(p);
        _squareStack.push(s);
        put(p, s);

    }

    /** Set square COL ROW to P. */
    final void put(Piece p, char col, char row) {
        put(p, sq(col - 'a', row - '1'));
    }

    /** Return true iff FROM - TO is an unblocked rook move on the current
     *  board.  For this to be true, FROM-TO must be a rook move and the
     *  squares along it, other than FROM, must be empty. */
    boolean isUnblockedMove(Square from, Square to) {
        if (from.col() == to.col()) {

            int min = Math.min(from.row(), to.row());
            int max = Math.max(from.row(), to.row());
            if (from.row() == min) {
                min = min + 1;
            } else {
                max = max - 1;
            }
            Piece intermediatePiece;
            for (int i = min; i <= max; i++) {
                intermediatePiece = get(from.col(), i);
                if (intermediatePiece != EMPTY) {
                    return false;
                }
            }
        } else if (from.row() == to.row()) {
            int min = Math.min(from.col(), to.col());
            int max = Math.max(from.col(), to.col());
            Piece intermediatePiece;
            if (from.col() == min) {
                min = min + 1;
            } else {
                max = max - 1;
            }
            for (int i = min; i <= max; i++) {
                intermediatePiece = get(i, from.row());
                if (intermediatePiece != EMPTY) {
                    return false;
                }
            }
        } else {
            return false;
        }

        return true;
    }

    /** Return true iff FROM is a valid starting square for a move. */
    boolean isLegal(Square from) {
        return get(from) == _turn;
    }
    /** Meant to iterate over the board's black adjacents.
     * @param to a square
     * @return a boolean
     * */
    boolean isThrone(Square to) {
        return ((to.col() == THRONE.col()) && (to.row() == THRONE.row()));
    }
    /** Return true iff FROM-TO is a valid move. */
    boolean isLegal(Square from, Square to) {

        Piece p = get(from);
        System.out.println(p.toString() + " " + this._turn);
        if (p == Piece.KING) {
            if (this._turn != WHITE) {
                return false;
            }
        } else if (p != this._turn) {
            return false;
        }

        if (get(from.col(), from.row()) == EMPTY) {
            return false;
        }
        if ((from.row() != to.row() && from.col() != to.col()) || from == to) {
            return false;
        }

        if (get(to.col(), to.row()) != EMPTY) {
            return false;
        }

        if ((p != KING) && (isThrone(to))) {
            return false;
        } else {
            return isUnblockedMove(from, to);
        }
    }

    /** Return true iff MOVE is a legal move in the current
     *  position. */
    boolean isLegal(Move move) {
        return isLegal(move.from(), move.to());
    }
    /** Meant to check adjancents.
     * @param sq a square example
     * @return a boolean
     * */
    boolean isAdjacentToThrone(Square sq) {
        int x = sq.col();
        int y = sq.row();
        for (int xi = x - 1; xi <= x + 1; xi++) {
            for (int yj = y - 1; yj <= y + 1; yj++) {
                if ((Square.exists(xi, yj)) && (sq(xi, yj).adjacent(sq))) {
                    if (isThrone(sq(xi, yj))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    /** Meant to check for a kill.
     * @param sq a square
     * */
    void checkKill(Square sq) {
        int x = sq.col();
        int y = sq.row();
        Piece p = get(x, y);
        Piece opponent = opponentPiece(p);
        Square left = null;
        if (Square.exists(x - 1, y)) {
            left = sq(x - 1, y);
        }
        Square right = null;
        Square top = null;
        Square bottom = null;
        if (Square.exists(x + 1, y)) {
            right = sq(x + 1, y);
        }
        if (Square.exists(x, y + 1)) {
            top = sq(x, y + 1);
        }
        if (Square.exists(x, y - 1)) {
            bottom = sq(x, y - 1);
        }
        if (p == KING) {
            if ((isThrone(sq)) || (isAdjacentToThrone(sq))) {
                if ((isHostile(p, left)) && (isHostile(p, right))
                        && (isHostile(p, bottom)) && (isHostile(p, top))) {
                    _winner = opponent;
                    _myboardelements[sq.col()][sq.row()] = EMPTY;
                }
            } else {
                captureAdjHostile(p, left, right, bottom, top);
                _winner = opponent;
                _myboardelements[sq.col()][sq.row()] = EMPTY;
            }
        } else if ((p == BLACK) || (p == WHITE)) {
            captureAdjHostile(p, left, right, bottom, top);

        }
    }
    /** Meant to iterate over the board's black adjacents.
     * @param p a piece
     * @param l a square
     * @param r a square
     * @param b a square
     * @param t a square
     * */
    void captureAdjHostile(Piece p, Square l, Square r, Square b, Square t) {
        if ((isHostile(p, l)) && (isHostile(p, r))) {
            capture(l, r);
        } else if ((isHostile(p, b)) && (isHostile(p, t))) {
            capture(b, t);
        }
    }
    /** Meant to iterate over the board's black adjacents.
     * @param p a piece
     * @param sq a square
     * @return a boolean
     * */
    boolean isHostile(Piece p, Square sq) {
        if (sq == null) {
            return false;
        }

        Piece pother = get(sq);
        if (isEnemy(p, pother)) {
            return true;
        } else {
            if (isThrone(sq)) {
                if (pother == EMPTY) {
                    return true;
                } else {
                    if (p == WHITE) {
                        if (numBlackAdjacentSquares(sq) == 3) {
                            return true;
                        }

                    }
                }
            }
        }
        return false;
    }
    /** Meant to iterate over the board's black adjacents.
     * @param to a square
     * @return an int
     * */
    int numBlackAdjacentSquares(Square to) {
        int x = to.col();
        int y = to.row();
        Piece pother;
        int numBlack = 0;
        for (int xi = x - 1; xi <= x + 1; xi++) {
            for (int yj = y - 1; yj <= y + 1; yj++) {
                if ((Square.exists(xi, yj)) && (sq(xi, yj).adjacent(to))) {
                    pother = get(sq(xi, yj));
                    if (pother == BLACK) {
                        numBlack++;
                    }
                }
            }
        }
        return numBlack;
    }
    /** Meant to iterate over the board's black adjacents.
     * @param p a piece
     * @param pother a piece
     * @return a boolean
     * */
    boolean isEnemy(Piece p, Piece pother) {
        if (p != pother) {
            return (((p == BLACK) && ((pother == WHITE) || (pother == KING)))
                    || (((p == WHITE) || (p == KING)) && (pother == BLACK)));
        }
        return false;
    }
    /** Meant to iterate over the board's black adjacents.
     * @param p a piece
     * @param to a square
     * */
    void checkEnemyCaptured(Piece p, Square to) {
        int x = to.col();
        int y = to.row();
        Piece pother;
        for (int xi = x - 1; xi <= x + 1; xi++) {
            for (int yj = y - 1; yj <= y + 1; yj++) {
                if ((Square.exists(xi, yj)) && (sq(xi, yj).adjacent(to))) {
                    pother = get(xi, yj);
                    if (isEnemy(p, pother)) {
                        checkKill(sq(xi, yj));
                    }
                }
            }
        }
    }
    /** Move FROM-TO, assuming this is a legal move. */
    void makeMove(Square from, Square to) {
        assert isLegal(from, to);
        if (n_limit > 0) {
            Piece p = get(from);
            Piece opponent = opponentPiece(p);
            if (moveCount() >= n_limit) {
                if (_winner == null) {
                    _winner = opponent;
                    recordPattern(opponent);
                }
            } else if (isLegal(from, to)) {
                _myboardelements[from.col()][from.row()] = EMPTY;
                put(p, to);
                _moveCount++;
                if (p == KING) {
                    if (to.isEdge()) {
                        _winner = p;
                    } else {
                        checkEnemyCaptured(p, to);
                    }
                } else if (p == BLACK) {
                    checkEnemyCaptured(p, to);
                } else if (p == WHITE) {
                    checkEnemyCaptured(p, to);
                } else {
                    System.out.println("Illegal from piece");
                }
                recordPattern(opponent);
                if (repeatedPosition()) {
                    if (opponent != EMPTY) {
                        _winner = opponent;
                    }
                } else {
                    _turn = opponent;
                }
            }
        } else {
            System.out.println("Set limit of game ");
        }
    }
    /** Meant to check the opponent pieces.
     * @param p a piece
     * @return a piece
     * */
    Piece opponentPiece(Piece p) {
        if ((p == WHITE) || (p == KING)) {
            return BLACK;
        } else if (p == BLACK) {
            return WHITE;
        } else {
            return p;
        }
    }
    /** Meant to check the opponent pieces.
     * @param opponent a piece
     * */
    void recordPattern(Piece opponent) {
        String key = this.toString();
        if (_previousStates == null) {
            _previousStates = new HashMap<String, BoardState>();
        }
        BoardState bs = _previousStates.get(key);
        if (bs == null) {
            bs = new BoardState();
            bs.boardstring = key;
            bs._moveCount = _moveCount;
            bs._turn = _turn;
            bs._myboardelements = _myboardelements;

            bs._nextturn = opponent;
            _previousStates.put(key, bs);
        } else {
            if (opponent == bs._nextturn) {
                _winner = opponent;
            }
            _repeated = true;

        }

    }
    /** Move according to MOVE, assuming it is a legal move. */
    void makeMove(Move move) {
        makeMove(move.from(), move.to());
    }

    /** Capture the piece between SQ0 and SQ2, assuming a piece just moved to
     *  SQ0 and the necessary conditions are satisfied. */
    private void capture(Square sq0, Square sq2) {

        Square mid = sq0.between(sq2);
        Piece p = get(mid.col(), mid.row());

        if (p == KING) {
            _winner = BLACK;
            _myboardelements[mid.col()][mid.row()] = EMPTY;
        } else {
            put(EMPTY, mid);
        }
    }

    /** Undo one move.  Has no effect on the initial board. */
    void undo() {
        if (_moveCount > 0) {
            undoPosition();
        }
    }

    /** Remove record of current position in the set of positions encountered,
     *  unless it is a repeated position or we are at the first move. */
    private void undoPosition() {
        this._previousStates.remove(this.toString());

        BoardState b = prevConfig.pop();

        this._myboardelements = b._myboardelements;
        this._moveCount = b._moveCount;
        this._turn = b._turn;
    }
    /** Meant to check the opponent pieces.
     * @param b a board
     * @param opponent piece
     * */
    public void setPreviousConfiguration(Board b, Piece opponent) {
        if (prevConfig == null) {
            prevConfig = new Stack<>();
        }
        BoardState bs = new BoardState();
        bs._moveCount = b._moveCount;
        bs._winner = b._winner;
        bs.n_limit = b.n_limit;
        bs._myboardelements = new Piece[SIZE][SIZE];
        Piece p;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                p = b._myboardelements[i][j];
                bs._myboardelements[i][j] = p;
            }
        }

        bs._turn = b._turn;
        bs._nextturn = opponent;
        bs.boardstring = b.toString();
        prevConfig.push(bs);
    }


    /** Clear the undo stack and board-position counts. Does not modify the
     *  current position or win status. */
    void clearUndo() {
    }

    /** Return a new mutable list of all legal moves on the current board for
     *  SIDE (ignoring whose turn it is at the moment). */
    List<Move> legalMoves(Piece side) {

        HashSet<Square> mypositions = pieceLocations(side);
        List<Move> movelist = null;
        for (Square sq : mypositions) {
            for (int xi = 0; xi < SIZE; xi++) {
                for (int yi = 0; yi < SIZE; yi++) {
                    if (Square.exists(xi, yi)) {
                        if (isLegal(sq, sq(xi, yi))) {
                            if (movelist == null) {
                                movelist = new ArrayList<Move>();
                            }
                            movelist.add(Move.mv(sq, sq(xi, yi)));
                        }
                    }
                }
            }
        }

        return movelist;
    }
    /** Return true iff SIDE has a legal move. */
    boolean hasMove(Piece side) {
        List<Move> mymoves = legalMoves(side);
        if (mymoves == null) {
            _winner = opponentPiece(side);
            return false;
        } else {
            return true;
        }

    }

    @Override
    public String toString() {
        return toString(true);
    }

    /** Return a text representation of this Board.  If COORDINATES, then row
     *  and column designations are included along the left and bottom sides.
     */
    String toString(boolean coordinates) {
        Formatter out = new Formatter();
        for (int r = SIZE - 1; r >= 0; r -= 1) {
            if (coordinates) {
                out.format("%2d", r + 1);
            } else {
                out.format("  ");
            }
            for (int c = 0; c < SIZE; c += 1) {
                out.format(" %s", get(c, r));
            }
            out.format("%n");
        }
        if (coordinates) {
            out.format("  ");
            for (char c = 'a'; c <= 'i'; c += 1) {
                out.format(" %c", c);
            }
            out.format("%n");
        }
        return out.toString();
    }

    /** Return the locations of all pieces on SIDE. */
    public HashSet<Square> pieceLocations(Piece side) {
        assert side != EMPTY;
        HashSet<Square> mypieceSquares = null;
        for (int i = 0; i < _myboardelements.length; i++) {
            for (int j = 0; j < _myboardelements[0].length; j++) {
                if (sameSide(_myboardelements[i][j], side)) {
                    if (mypieceSquares == null) {
                        mypieceSquares = new HashSet<Square>();
                    }
                    mypieceSquares.add(sq(i, j));
                }
            }

        }
        return mypieceSquares;
    }
    /** Meant to check the opponent pieces.
     * @param p a piece
     * @param side piece
     * @return a boolean
     * */
    boolean sameSide(Piece p, Piece side) {
        if ((side == WHITE) || (side == KING)) {
            return ((p == WHITE) || (p == KING));
        } else {
            return p == side;
        }
    }
    /** Return the contents of _board in the order of SQUARE_LIST as a sequence
     *  of characters: the toString values of the current turn and Pieces. */
    String encodedBoard() {
        char[] result = new char[Square.SQUARE_LIST.size() + 1];
        result[0] = turn().toString().charAt(0);
        for (Square sq : SQUARE_LIST) {
            result[sq.index() + 1] = get(sq).toString().charAt(0);
        }
        return new String(result);
    }

    /** Piece whose turn it is (WHITE or BLACK). */
    private Piece _turn;
    /** Cached value of winner on this board, or EMPTY if it has not been
     *  computed. */
    private Piece _winner;
    /** Number of (still undone) moves since initial position. */
    private int _moveCount;
    /** True when current board is a repeated position (ending the game). */
    private boolean _repeated;

    /**This is the limit of moves.**/
    private int n_limit = 50;

    /**This is my board.**/
    private Piece[][] _myboardelements;
    /**This is a stack of squares.**/
    private Stack<Square> _squareStack;
    /**This is a stack of pieces.**/
    private Stack<Piece> _pieceStack;
    /**This is a hashmap of previous states of board.**/
    private HashMap<String, BoardState> _previousStates;

    /**This map will help memorize my previous overall board configuration.**/
    private Stack<BoardState> prevConfig;
}
class BoardState {

    Piece _nextturn;
    String boardstring;
    Piece[][] _myboardelements;
    int _moveCount;
    Piece _winner;
    int n_limit;
    Piece _turn;
}
