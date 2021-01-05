package enigma;

import static enigma.EnigmaException.error;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Enigma simulator.
 * @author Razi Mahmood
 */
public final class Main {

    /**
     * Process a sequence of encryptions and decryptions, as
     * specified by ARGS, where 1 <= ARGS.length <= 3.
     * ARGS[0] is the name of a configuration file.
     * ARGS[1] is optional; when present, it names an input file
     * containing messages. Otherwise, input comes from the standard
     * input. ARGS[2] is optional; when present, it names an output
     * file for processed messages. Otherwise, output goes to the
     * standard output. Exits normally if there are no errors in the input;
     * otherwise with code 1.
     */
    public static void main(String... args) {
        try {
            new Main(args).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Check ARGS and open the necessary files (see comment on main). */
    Main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            throw error("Only 1, 2, or 3 command-line arguments allowed");
        }
        _config = getInput(args[0]);

        if (args.length > 1) {
            _input = getInput(args[1]);
        } else {
            _input = new Scanner(System.in);
        }
        if (args.length > 2) {
            _output = getOutput(args[2]);
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /**
     * Configure an Enigma machine from the contents of configuration
     * file _config and apply it to the messages in _input, sending the
     * results to _output.
     */
    private void process() {

        Machine mymachine = readConfig();
        processInput(mymachine);

        _output.close();
    }

    /**Used to get the setting of a rotor from the input file.
     * @param word the line to focus on from the input file
     * @param rotornamelist list of rotor names
     * @return the new setting
     **/
    String getSetting(String word, ArrayList<String> rotornamelist) {

        String setting = word;
        if (setting.length() < rotornamelist.size() - 1) {
            throw new EnigmaException("Wheel settings too short");
        } else if (setting.length() >= rotornamelist.size()) {
            throw new EnigmaException("Wheel settings too long");
        } else {
            for (int j = 0; j < setting.length(); j++) {
                if (!_alphabet.contains(setting.charAt(j))) {
                    throw new EnigmaException("Bad character in wheel setting");
                }
            }
        }
        return setting;
    }
    /**Used to add to the plugboard.
           * @param word the line to focus on from the input file
           * @param plugboard the plugboard
           * @return the new plugboard
           **/
    String addToPlugboard(String word, String plugboard) {
        if (plugboard == null) {
            plugboard = word;
        } else {
            plugboard += " " + word;
        }
        return plugboard;
    }
    /**Used to get the next word.
           * @param linetokens array of tokens
           * @param i index
           * @return the next word
           **/
    String getNextWord(String[]linetokens, int i) {
        String nextword;
        if (i < linetokens.length - 1) {
            nextword = linetokens[i + 1];
        } else {
            nextword = null;
        }
        return nextword;
    }
    /**Used to process the odd lines taken directly from the input file.
     * @param directiveline the input line
     * @param mymachine current Machine
     **/
    void processDirective(String directiveline, Machine mymachine) {
        ArrayList<String> rotornamelist = new ArrayList<String>();
        String[] linetokens = directiveline.split(" ");
        String  typename, word, firstchar;
        HashSet<String> rotorSet = new HashSet<String>();
        int maxRotors = mymachine.numRotors();
        String nextword, settings = null;
        String plugboard = "";
        for (int i = 0; i < linetokens.length; i++) {
            word = linetokens[i];
            nextword = getNextWord(linetokens, i);
            if (i == 0) {
                firstchar = word;
                if (!firstchar.equals("*")) {
                    throw new EnigmaException("Bad directive line");
                }
            } else if (i <= maxRotors) {
                typename = rotorTypeMap.get(word);
                if (typename == null) {
                    if ((nextword != null)
                            && (rotorTypeMap.get(nextword) != null)) {
                        throw new EnigmaException("Bad rotor name");
                    } else {
                        if (settings == null) {
                            settings = getSetting(word, rotornamelist);
                        } else {
                            plugboard = addToPlugboard(word, plugboard);
                        }
                    }
                } else {
                    if (!rotorSet.contains(word)) {
                        rotorSet.add(word);
                        rotornamelist.add(word);
                    } else {
                        throw new EnigmaException("Duplicate rotor name");
                    }
                }
            } else {
                if (settings == null) {
                    settings = getSetting(word, rotornamelist);
                } else {
                    plugboard = addToPlugboard(word, plugboard);
                }
            }
        }
        Permutation myplugboard = new Permutation(plugboard, _alphabet);
        mymachine.setPlugboard(myplugboard);
        if (rotornamelist != null) {
            String[] rotornames = new String[rotornamelist.size()];
            rotornamelist.toArray(rotornames);
            mymachine.insertRotors(rotornames);
            setUp(mymachine, settings);
        } else {
            throw new EnigmaException("Empty rotor list");
        }
    }


    /**Used to process the lines taken directly from the input file.
     *  @param mymachine my current machine
     **/
    private void processInput(Machine mymachine) {
        String line;
        String myoutput;
        boolean directiveFound = false;
        boolean atleastOneStringToConvert = false;
        while (_input.hasNext()) {
            line = _input.nextLine();
            line = line.trim();

            if (!line.equals("")) {
                if (line.startsWith("*")) {
                    line = stripLine(line);
                    processDirective(line, mymachine);
                    directiveFound = true;
                } else if (directiveFound) {
                    myoutput = mymachine.convert(line);
                    atleastOneStringToConvert = true;
                    printMessageLine(myoutput);
                }
            } else {
                printMessageLine("");
            }
        }
        if (!directiveFound) {
            throw new EnigmaException("Invalid input file: No directive given");
        } else if (!atleastOneStringToConvert) {
            throw new EnigmaException("Wrong number of arguments");
        }
    }

    /**Used to remove any extra tabs or newlines from config file.
     * @param line the config line
     * @return a new line
     * **/
    String stripLine(String line) {
        String newline = line;
        newline = newline.replace("\t", " ");
        newline = newline.replace("\r\n", " ");
        newline = newline.replace("\n", " ");
        newline = newline.trim();

        String outline = "";

        for (int i = 0; i < newline.length(); i++) {
            if (i == 0) {
                outline += newline.charAt(i);
            } else if (!((newline.charAt(i) == ' ')
                    && (newline.charAt(i - 1) == ' '))) {
                outline += newline.charAt(i);
            }
        }
        outline = outline.trim();
        return outline;
    }

    /**
     * Return an Enigma machine configured from the contents of
     * configuration file _config.
     * @param line the config line
     * @return a new rotor
     */
    Rotor readRotorLine(String line) {
        String name;
        char rotortype;
        String notches = "";
        String cycles = "";
        line = stripLine(line);
        String[] linetokens = line.split(" ");

        name = linetokens[0];

        String rotortypestring = linetokens[1];
        rotortype = rotortypestring.charAt(0);
        if (rotortypestring.length() > 1) {
            notches = rotortypestring.substring(1, rotortypestring.length());
        }

        for (int j = 2; j < linetokens.length; j++) {
            cycles += linetokens[j];
        }
        return makeRotor(name, notches, cycles, rotortype);
    }

    /** Return a rotor, reading its description from _config.
     * @param name rotor name
     * @param notch notch
     * @param cycles the cycles
     * @param rotorType config line
     * @return new rotor
     * **/
    Rotor makeRotor(String name, String notch, String cycles, char rotorType) {
        Rotor rotor = null;
        Permutation perm = new Permutation(cycles, _alphabet);

        if (rotorTypeMap == null) {
            rotorTypeMap = new HashMap<String, String>();
        }
        rotorTypeMap.put(name, "" + rotorType);

        if (rotorType == 'M') {
            rotor = new MovingRotor(name, perm, notch);
        } else if (rotorType == 'N') {
            rotor = new FixedRotor(name, perm);
        } else if (rotorType == 'R') {
            rotor = new Reflector(name, perm);
        } else {
            throw new EnigmaException("Invalid rotor type");
        }
        return rotor;
    }
    /**Checks any occurences of whitespaces.
     * @param c the passed in character
     * @return a boolean
     * */
    boolean isWhiteSpace(char c) {
        return ((c == ' ') || (c == '\t') || (c == '\n'));
    }

    /**
     * Set M according to the specification given on SETTINGS,
     * which must have the format specified in the assignment.
     */
    private void setUp(Machine M, String settings) {
        if (settings == null) {
            throw new EnigmaException("no settings available");
        }
        M.setRotors(settings);
    }

    /**
     * Print MSG in groups of five (except that the last group may
     * have fewer letters).
     */
    private void printMessageLine(String msg) {
        String group = null;
        String newmsg = msg.replace(" ", "");
        if (newmsg.equals("")) {
            _output.println();
        } else {
            int min = 0;
            for (int i = 0; i < newmsg.length(); i += 5) {
                min = Math.min(i + 5, newmsg.length());
                if (group == null) {
                    group = newmsg.substring(i, min);
                } else {
                    group += " " + newmsg.substring(i, min);
                }
            }
            _output.println(group);
        }
    }
    /**Stores all the lines of the config file.
     * @return a new arraylist
     * */
    ArrayList<String> getAllLines() {
        ArrayList<String> allLines = null;
        String line = null;
        String assembledLine = null;
        boolean foundStart = false;
        boolean foundEnd = false;

        while (_config.hasNext()) {
            line = _config.nextLine();
            line = line.trim();
            if (!line.startsWith("(")) {
                if (assembledLine != null) {
                    if (allLines == null) {
                        allLines = new ArrayList<String>();
                    }
                    allLines.add(assembledLine);
                }
                assembledLine = line;
            } else if (!line.equals("")) {
                if (assembledLine != null) {
                    assembledLine += line;
                } else {
                    throw new EnigmaException("Ill-formed rotor description");
                }
            }
        }

        if (assembledLine != null) {
            if (allLines == null) {
                allLines = new ArrayList<String>();
            }
            allLines.add(assembledLine);
        }
        return allLines;
    }
    /**Reads through the alphabet in the line.
     * @param line the string line
     * */
    void readAlphabet(String line) {
        if (line != null) {
            _alphabet = new Alphabet(line.trim());
        } else {
            throw new EnigmaException("No alphabet line given");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        if (_currRotorLine != null) {
            return readRotorLine(_currRotorLine);
        } else {
            throw new EnigmaException("No rotor line");
        }
    }

    /**Used to read through each rotor on every config line.
     * @param allLines the list of all lines
     * @return a new collection of rotors
     * **/
    Collection<Rotor> readRotors(ArrayList<String> allLines) {
        String line;
        Rotor r;
        Collection<Rotor> possibleRotors = new ArrayList<Rotor>();
        for (int j = 2; j < allLines.size(); j++) {
            _currRotorLine = allLines.get(j);
            r = readRotor();
            possibleRotors.add(r);
        }
        return possibleRotors;
    }

    /**Used to read into the config file and generate a Machine.
     * @return a new machine
     * **/
    private Machine readConfig() {
        try {
            ArrayList<String> allLines = getAllLines();
            if (allLines == null) {
                throw new EnigmaException("No lines in the configuration file");
            }
            readAlphabet(allLines.get(0));

            String line;
            int numRotors = 0;
            int numPawls = 0;
            char r, p;

            String[] lineTokens;
            if (allLines.size() > 1) {
                line = allLines.get(1);
                if (line != null) {
                    line = line.trim();
                    String validCharacters = "";
                    for (int i = 0; i < line.length(); i++) {
                        if (!isWhiteSpace(line.charAt(i))) {
                            validCharacters += line.charAt(i);
                        }
                    }
                    if (validCharacters.length() > 1) {
                        r = validCharacters.charAt(0);
                        p = validCharacters.charAt(1);

                        try {
                            numRotors = Integer.parseInt("" + r);
                            numPawls = Integer.parseInt("" + p);
                        } catch (NumberFormatException e) {
                            throw new EnigmaException("ill formed config file");
                        }
                    } else {
                        throw new EnigmaException("Invalid # of rotors/pawls");
                    }
                }
            } else {
                throw new EnigmaException("No pawl and rotor line given");
            }
            Collection<Rotor> possibleRotors = readRotors(allLines);
            return new Machine(_alphabet, numRotors, numPawls, possibleRotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;
    /** Source of input messages. */
    private Scanner _input;
    /** Source of machine configuration. */
    private Scanner _config;
    /** File for encoded/decoded messages. */
    private PrintStream _output;
    /**Used to check the rotor lines.**/
    private String _currRotorLine;
    /**Used to check the rotor types.**/
    private HashMap<String, String> rotorTypeMap;
}
