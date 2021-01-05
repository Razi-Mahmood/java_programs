package enigma;

import java.util.HashSet;
import static enigma.EnigmaException.*;
/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *  @author Razi Mahmood
 */
class Alphabet {

    /** A new alphabet containing CHARS.  Character number #k has index
     *  K (numbering from 0). No character may be duplicated. */
    private String alphabetString;
    /**A hashset used to keep track of duplicates.**/
    private HashSet<Character> charset = new HashSet<Character>();;

    /**Meant to identify any possible duplicates.
     * @param chars collection of characters
     * **/

    Alphabet(String chars) {
        if (chars != null) {
            for (int i = 0; i < chars.length(); i++) {
                charset.add(chars.charAt(i));
            }
            if (charset.size() == chars.length()) {
                alphabetString = chars;
            } else {
                throw new EnigmaException("duplicates visible, chars invalid");
            }
        } else {
            throw new EnigmaException("empty alphabet given");
        }
    }

    /** A default alphabet of all upper-case characters. */
    Alphabet() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /** Returns the size of the alphabet. */
    int size() {
        return alphabetString.length();
    }

    /** Returns true if preprocess(CH) is in this alphabet. */
    boolean contains(char ch) {
        return (alphabetString.indexOf(ch) >= 0);
    }

    /** Returns character number INDEX in the alphabet, where
     *  0 <= INDEX < size(). */
    char toChar(int index) {
        if ((index >= 0) && (index < size())) {
            return alphabetString.charAt(index);
        } else {
            throw new EnigmaException("index is out of range");
        }
    }

    /** Returns the index of character preprocess(CH), which must be in
     *  the alphabet. This is the inverse of toChar(). */
    int toInt(char ch) {
        int index = alphabetString.indexOf(ch);
        if (index >= 0) {
            return index;
        } else {
            throw new EnigmaException(ch + " not in the alphabet");
        }
    }
}
