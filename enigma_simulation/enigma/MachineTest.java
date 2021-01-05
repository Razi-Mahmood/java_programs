package enigma;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import static enigma.TestUtils.*;
import static org.junit.Assert.*;

public class MachineTest {

    @Test
    public void checkRotorDescription() {
        ArrayList<Rotor> rotorlist = new ArrayList<Rotor>();
        rotorlist.add(getRotor("I", NAVALA, ""));
        rotorlist.add(getRotor("II", NAVALA, ""));
        rotorlist.add(getRotor("III", NAVALA, ""));
        rotorlist.add(getRotor("IV", NAVALA, ""));
        rotorlist.add(getRotor("Beta", NAVALA, ""));
        rotorlist.add(getRotor("Gamma", NAVALA, ""));
        rotorlist.add(getRotor("B", NAVALA, ""));

        Machine m = new Machine(UPPER, 5, 3, rotorlist);
        String directiveline = "* B Beta III IV I AXLE (YV) (HK)";
        String inputString = "Hello World";
        m.processDirective(directiveline);
        assertEquals(msg("1", "mismatch in # specified + given rotors %d %d",
                m.numRotors(), m._storeRotors.length),
                m.numRotors(), m._storeRotors.length);
    }

    @Test
    public void checkConvert() {

        String inputString = "HELLO WORLD";

        ArrayList<Rotor> rotorlist = new ArrayList<Rotor>();
        rotorlist.add(getRotor("I", NAVALA, ""));
        rotorlist.add(getRotor("II", NAVALA, ""));
        rotorlist.add(getRotor("III", NAVALA, ""));
        rotorlist.add(getRotor("IV", NAVALA, ""));
        rotorlist.add(getRotor("Beta", NAVALA, ""));
        rotorlist.add(getRotor("Gamma", NAVALA, ""));
        rotorlist.add(getRotor("B", NAVALA, ""));
        Machine m = new Machine(UPPER, 5, 3, rotorlist);

        String directiveline = "* B Beta III IV I AXLE (YV) (HK)";

        m.processDirective(directiveline);
        assertEquals(msg("1", "mismatch in # of specified + given rotors %d %d",
                m.numRotors(), m._storeRotors.length),
                m.numRotors(), m._storeRotors.length);

        String decodedString = m.convert(inputString);
        String expectedString = "OKYGJ ZUHSG";

        assertEquals(expectedString, decodedString);
    }

    private Rotor getRotor(String name, HashMap<String, String> rotors,
                           String notches) {
        return new MovingRotor(name,
                new Permutation(rotors.get(name), UPPER), notches);
    }
}
