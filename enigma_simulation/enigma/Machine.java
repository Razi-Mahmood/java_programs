package enigma;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Razi Mahmood
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        if (numRotors > allRotors.size()) {
            throw new EnigmaException("#rotors exceeds #available rotors");
        }
        _numRotors = numRotors;
        _pawls = pawls;
        _allPossibleRotors = allRotors;
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        if (rotors == null) {
            throw new EnigmaException("No rotors to insert given");
        } else if (rotors.length > _numRotors) {
            throw new EnigmaException("Length of rotors is inconsistent");
        }

        int minRotorLength = Math.min(_numRotors, rotors.length);
        ArrayList<Rotor> tempRotors = null;
        _storeRotors = new Rotor[minRotorLength];
        Rotor testRotor;

        for (int i = 0; i < minRotorLength; i++) {
            testRotor = extractRotor(rotors[i]);

            if (testRotor != null) {

                if (i == 0) {
                    if (!isReflector(testRotor)) {
                        throw new EnigmaException("leftmost is not reflector");
                    }
                } else if ((i >= 1) && (i < (minRotorLength - 1))) {
                    if (isReflector(testRotor)) {
                        throw new EnigmaException("Reflector in middle");
                    }
                } else if (i == (minRotorLength - 1)) {
                    if (!isMovingRotor(testRotor)) {
                        throw new EnigmaException("rightmost should move");
                    }
                }
                if (tempRotors == null) {
                    tempRotors = new ArrayList<Rotor>();
                }
                tempRotors.add(testRotor);
            }
            if (tempRotors != null) {
                _storeRotors = new Rotor[tempRotors.size()];
                _storeRotors = tempRotors.toArray(_storeRotors);
            }
        }
    }

    /**Meant to extract the specific Rotor based on the passed in name.
     @param name of rotor
     @return test **/
    Rotor extractRotor(String name) {
        Iterator it = _allPossibleRotors.iterator();
        Rotor test = null;
        boolean found = false;

        while ((!found) && (it.hasNext())) {
            test = (Rotor) it.next();
            if (test.name().equals(name)) {
                found = true;
            }
        }
        if (found) {
            return test;
        } else {
            return null;
        }
    }
    /**Used to process the odd lines taken directly from the input file.
     * @param directiveline new line from file
     * **/
    void processDirective(String directiveline) {
        String[] rotornames = new String[this.numRotors()];
        String[] linetokens = directiveline.split(" ");
        for (int i = 1; i < rotornames.length + 1; i++) {
            rotornames[i - 1] = linetokens[i];
        }
        String settings = linetokens[rotornames.length + 1];
        String plugboard = "";
        for (int i = rotornames.length + 2; i < linetokens.length; i++) {
            plugboard += linetokens[i];
        }
        Permutation myplugboard = new Permutation(plugboard, _alphabet);
        setPlugboard(myplugboard);
        insertRotors(rotornames);
        setRotors(settings);
    }


    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        char c = ' ';
        for (int i = 0; i < setting.length(); i++) {
            c = setting.charAt(i);
            _storeRotors[i + 1].set(c);
        }
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Create a character mapping for the plugboard.
     * @param c character to map
     * @return new int from alphabet
     * */
    int mapInPlugboard(char c) {
        char d = _plugboard.permute(c);
        return _alphabet.toInt(d);
    }
    /**Check if the rotor is moving.
     * @param rotor test rotor
     * @return check type
     * */
    boolean isMovingRotor(Rotor rotor) {
        return MovingRotor.class.isInstance(rotor);
    }
    /**Check if the rotor is fixed.
     * @param rotor test rotor
     * @return check type
     * */
    boolean isFixedRotor(Rotor rotor) {
        return FixedRotor.class.isInstance(rotor);
    }
    /**Check if the rotor is a reflector.
     * @param rotor test rotor
     * @return check type
     * */
    boolean isReflector(Rotor rotor) {
        return Reflector.class.isInstance(rotor);
    }

    /**Check if the rotor is the rightmost.
     * @param x index
     * @return check type
     * */
    boolean isRightmostRotor(int x) {
        return (x == _storeRotors.length - 1);
    }

    /**Used to handle whether a rotor and it's left neighbor can move.
     * @param cmap mappedcharacter
     * @return another mapped C
     */
    int moveForward(int cmap) {
        Rotor rotor;
        boolean[] hasAdvanced = new boolean[_storeRotors.length];
        for (int i = 0; i < _storeRotors.length; i++) {
            hasAdvanced[i] = false;
        }
        for (int i = _storeRotors.length - 1; i > 0; i--) {
            rotor = _storeRotors[i];
            if (isMovingRotor(rotor)) {
                if (isRightmostRotor(i)) {
                    if (rotor.atNotch()) {
                        rotor.advance();
                        if (isMovingRotor(_storeRotors[i - 1])) {
                            _storeRotors[i - 1].advance();
                            hasAdvanced[i - 1] = true;
                        }
                    } else {
                        rotor.advance();
                    }
                } else {
                    if ((!hasAdvanced[i]) && (rotor.atNotch())) {
                        if (isMovingRotor(_storeRotors[i - 1])) {
                            rotor.advance();
                            _storeRotors[i - 1].advance();
                            hasAdvanced[i - 1] = true;
                        }
                    }
                }
                cmap = rotor.convertForward(cmap);
            } else if (isFixedRotor(rotor)) {
                cmap = rotor.convertForward(cmap);
            }
        }
        return cmap;
    }

    /**Used to convert the rotor backward after hitting the reflector.
     * @param cmap mappedcharacter
     * @return another mapped char
     * */
    int moveBackward(int cmap) {
        Rotor rotor;
        for (int i = 1; i < _storeRotors.length; i++) {
            rotor = _storeRotors[i];
            cmap = rotor.convertBackward(cmap);
        }
        return cmap;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {

        int mappedC = mapInPlugboard(_alphabet.toChar(c));
        mappedC = moveForward(mappedC);

        mappedC = _storeRotors[0].convertForward(mappedC);
        mappedC = moveBackward(mappedC);
        int outputC = mapInPlugboard(_alphabet.toChar(mappedC));
        return outputC;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        char c;
        if (msg == null) {
            return msg;
        }
        if (msg.equals("")) {
            return msg;
        }

        String convertedString = "";
        char add;
        for (int i = 0; i < msg.length(); i++) {
            c = msg.charAt(i);
            if (_alphabet.contains(c)) {
                add = _alphabet.toChar(convert(_alphabet.toInt(c)));
                convertedString += add;
            } else {
                convertedString += c;
            }
        }
        return convertedString;
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;
    /** Number of Rotors. */
    private int _numRotors;
    /** Meant to store the number of Pawls. */
    private int _pawls;
    /** Collection of all possible rotors. */
    private Collection<Rotor> _allPossibleRotors;

    /** Rotor array of stored rotors. */
    protected Rotor[] _storeRotors;
    /** A new plugboard to set. */
    private Permutation _plugboard;
}
