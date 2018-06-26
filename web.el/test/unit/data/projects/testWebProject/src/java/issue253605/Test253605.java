
package issue253605;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

/**
 *
 * @author petr
 */
@ManagedBean
@RequestScoped
public class Test253605 {

    public String foo(Test253605Enum e) {
        return e.name();
    }
}
