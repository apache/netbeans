
package test;

import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import javax.ejb.FinderException;


/**
 * This is the home interface for EntityA enterprise bean.
 */
public interface EntityARemoteHome extends EJBHome {
    
    EntityARemote findByPrimaryKey(String key)  throws FinderException, RemoteException;
    
    EntityARemote create(String key)  throws CreateException, RemoteException;
    
    
}
