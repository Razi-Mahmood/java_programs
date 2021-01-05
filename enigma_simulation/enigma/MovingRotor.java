package enigma;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author Razi Mahmood
 */
class MovingRotor extends Rotor {

    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initally in its 0 setting (first character of its
     *  alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        _notches = notches;
    }

    @Override
    void advance() {
        set(this.setting() + 1);
    }
    @Override
    boolean atNotch() {
        boolean found = false;
        int i = 0;
        char cNotch;

        while (!found && (i < _notches.length())) {
            cNotch = _notches.charAt(i);
            if (this.permutation().alphabet().toInt(cNotch) == this.setting()) {
                found = true;
            } else {
                i++;
            }
        }
        return found;
    }
    @Override
    boolean rotates() {
        return true;
    }

    /**This variable stores the notches String being passed in.**/
    private String _notches;

}
