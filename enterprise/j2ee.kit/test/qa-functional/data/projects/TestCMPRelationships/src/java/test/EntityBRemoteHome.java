
package test;

import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import javax.ejb.FinderException;


/**
 * This is the home interface for EntityB enterprise bean.
 */
public interface EntityBRemoteHome extends EJBHome {
    
    EntityBRemote findByPrimaryKey(String key)  throws FinderException, RemoteException;
    
    EntityBRemote create(String key)  throws CreateException, RemoteException;
    
    
}
