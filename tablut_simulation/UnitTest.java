package tablut;

import org.junit.Test;
import static org.junit.Assert.*;
import ucb.junit.textui;

/** The suite of all JUnit tests for the enigma package.
 *  @author
 */
public class UnitTest {

    /** Run the JUnit tests in this package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTest.class);
    }

    /** A dummy test as a placeholder for real ones. */
    @Test
    public void testCopy() {

        Board test1 = new Board();
        Board copied1 = new Board();
        copied1.copy(test1);

        //assertEquals(test1, copied1);
    }
    @Test
    public void testMakeMove() {
        Board a = new Board();

        System.out.println(a);

        a.makeMove(Move.mv("h5-6"));
        System.out.println(a);
        a.makeMove(Move.mv("e4-b"));
        System.out.println(a);
        a.makeMove(Move.mv("h6-7"));
        System.out.println(a);
        a.makeMove(Move.mv("e5-4"));
        System.out.println(a);

        a.makeMove(Move.mv("h7-8"));
        System.out.println(a);
        a.makeMove(Move.mv("e4-h"));
        System.out.println(a);

    }
    @Test
    public void testCapture() {

        Board a = new Board();

        System.out.println(a);

        a.makeMove(Move.mv("e7-g"));
        System.out.println(a);
        a.makeMove(Move.mv("f9-7"));
        System.out.println(a);
        a.makeMove(Move.mv("e6-d"));
        System.out.println(a);
        a.makeMove(Move.mv("h5-7"));
        System.out.println(a);

    }

    @Test
    public void testUndo() {

        Board b = new Board();

        System.out.println(b);

        b.makeMove(Move.mv("f9-h"));
        System.out.println(b);

        b.undo();
        System.out.println(b);
    }









}


