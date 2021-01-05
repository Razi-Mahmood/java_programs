package enigma;

import java.util.ArrayList;
import java.util.HashSet;

import static enigma.EnigmaException.*;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Razi Mahmood
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        _cycles = cycles;

        addCycle(_cycles);
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {

        if (cycle == null) {
            throw new EnigmaException("empty cycle");
        } else if (cycle.equals("")) {
            return;
        }

        String term = "";
        char c;
        int countalphabet = 0;
        HashSet<Character> charset = null;

        for (int i = 0; i < cycle.length(); i++) {
            c = cycle.charAt(i);
            if (charset == null) {
                charset = new HashSet<Character>();
            }
            if (_alphabet.contains(c)) {
                countalphabet++;
                charset.add(c);
            } else if (!((c == '(') || (c == ')') || (c == ' '))) {
                throw new EnigmaException("invalid characters");
            }

        }
        if (charset == null) {
            throw new EnigmaException("improper cycle string or empty");
        } else {
            if (charset.size() != countalphabet) {
                throw new EnigmaException("duplicate letters from alphabet");
            }
        }
        boolean begincycle = true;
        for (int i = 0; i < cycle.length(); i++) {
            c = cycle.charAt(i);
            if (c == '(') {
                if (begincycle) {
                    begincycle = false;
                    term = "";
                } else {
                    throw new EnigmaException("invalid cycle");
                }
            } else if (c == ')') {

                if (term.equals("")) {
                    throw new EnigmaException("Empty cycle");
                } else {
                    if (permList == null) {
                        permList = new ArrayList<String>();
                    }
                    permList.add(term);
                    begincycle = true;
                }
            } else if (_alphabet.contains(c)) {
                term += c;
            }
        }
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {

        int pWrap = wrap(p);
        char c = _alphabet.toChar(pWrap);
        return _alphabet.toInt(permute(c));

    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {

        int cWrap = wrap(c);
        char in = _alphabet.toChar(cWrap);
        return _alphabet.toInt(invert(in));
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        String singleCycle;
        if (permList != null) {
            for (int j = 0; j < permList.size(); j++) {
                singleCycle = permList.get(j);
                for (int i = 0; i < singleCycle.length(); i++) {
                    if (singleCycle.charAt(i) == p) {
                        if (i == (singleCycle.length() - 1)) {
                            return singleCycle.charAt(0);
                        } else {
                            return singleCycle.charAt(i + 1);
                        }
                    }
                }
            }
        }
        return p;
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {

        String singleCycle;
        if (permList != null) {
            for (int j = 0; j < permList.size(); j++) {
                singleCycle = permList.get(j);

                for (int i = singleCycle.length() - 1; i >= 0; i--) {
                    if (singleCycle.charAt(i) == c) {
                        if (i == 0) {
                            return singleCycle.charAt(singleCycle.length() - 1);
                        } else {
                            return singleCycle.charAt(i - 1);
                        }
                    }
                }
            }
        }
        return c;
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        if (permList == null) {
            return true;
        } else {
            boolean found = false;
            int i = 0;
            String singleCycle = null;
            while ((!found) && (i < permList.size())) {
                singleCycle = permList.get(i);
                if (singleCycle.length() == 1) {
                    found = true;
                } else {
                    i++;
                }
            }
            return (!found);
        }
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;
    /** String to store cycles. */
    private String _cycles;
    /** ArrayList to store the permutations. */
    private ArrayList<String> permList;

}
