
package test;

import java.rmi.RemoteException;


/**
 * This is the business interface for EntityB enterprise bean.
 */
public interface EntityBRemoteBusiness {
    public abstract String getKey() throws RemoteException;
    
}
