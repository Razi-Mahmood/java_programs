package enigma;

import org.junit.Test;
import ucb.junit.textui;

import static org.junit.Assert.*;

/** The suite of all JUnit tests for the enigma package.
 *  @author
 */
public class UnitTest {

    /** Run the JUnit tests in this package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */

    public static void main(String[] ignored) {
        textui.runClasses(PermutationTest.class, MovingRotorTest.class);
    }

    @Test
    public static void testAlphabetMethods() {

        Alphabet alphabet = new Alphabet("THEKIUYOQABP");

        assertEquals(12, alphabet.size());
        assertEquals(true, alphabet.contains('E'));
        assertNotEquals(true, alphabet.contains('C'));


    }

}


