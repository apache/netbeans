
package test;

import java.rmi.RemoteException;


/**
 * This is the business interface for EntityA enterprise bean.
 */
public interface EntityARemoteBusiness {
    public abstract String getKey() throws RemoteException;
    
}
