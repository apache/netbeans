/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.faces.event.ActionEvent;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class Bean {

    public List<String> any() {
        return Arrays.asList(
                "Jeden",
                "Dva");
    }

    public String getProperty() {
        return "property";
    }

    public String[] getMyArray() {
        return new String[0];
    }

    public Iterable<String> getMyIterable() {
        return Collections.<String>emptyList();
    }

    public List<String> getMyList() {
        return Collections.<String>emptyList();
    }

    public String getMyString() {
        return "string";
    }

    public String getMyStringWithParam(String string) {
        return string;
    }

    public Map<String, String> getMyMap() {
        return Collections.<String, String>emptyMap();
    }

    public Cypris getMyCypris() {
        return new Cypris();
    }

    public void updateGameList(ActionEvent event) {
    }

}
