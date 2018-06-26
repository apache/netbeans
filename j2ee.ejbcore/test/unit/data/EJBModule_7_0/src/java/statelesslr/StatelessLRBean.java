/*
 * StatelessBean.java
 *
 * Created on Jan 28, 2013, 11:20:55 PM
 */

package statelesslr;

import javax.ejb.Stateless;
import javax.ejb.Remote;

/**
 *
 * @author klingo
 */
@Stateless
@Remote
public class StatelessBean implements StatelessLRRemote2, StatelessLRRemote {
    
    // Add business logic below. (Right-click in editor and choose
    // "EJB Methods > Add Business Method" or "Web Service > Add Operation")
 
}
