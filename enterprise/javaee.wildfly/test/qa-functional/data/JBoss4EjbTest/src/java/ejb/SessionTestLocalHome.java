
package ejb;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;


/**
 * This is the local-home interface for SessionTest enterprise bean.
 */
public interface SessionTestLocalHome extends EJBLocalHome {

    SessionTestLocal create()  throws CreateException;


}
