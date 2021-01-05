package gitlet;

import java.io.Serializable;
/**Meant to represent the head info.
 * @author Razi Mahmood
 * */

public class HeadInfo implements Serializable {
    /**current head hash.*/
    String headcode;
    /**current branch.*/
    String currbranchname;
}
