
package ejb;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;


/**
 * This is the local-home interface for SessionTest2 enterprise bean.
 */
public interface SessionTest2LocalHome extends EJBLocalHome {
    
    SessionTest2Local create()  throws CreateException;
    
    
}
