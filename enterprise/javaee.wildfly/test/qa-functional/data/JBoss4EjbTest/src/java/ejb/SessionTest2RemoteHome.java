
package ejb;

import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;


/**
 * This is the home interface for SessionTest2 enterprise bean.
 */
public interface SessionTest2RemoteHome extends EJBHome {

    SessionTest2Remote create()  throws CreateException, RemoteException;


}
