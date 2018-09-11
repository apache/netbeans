
package ejb;


/**
 * This is the home interface for AccountState enterprise bean.
 */
public interface AccountStateRemoteHome extends javax.ejb.EJBHome {
    
    
    
    /**
     *
     */
    ejb.AccountStateRemote create()  throws javax.ejb.CreateException, java.rmi.RemoteException;
    
    
}
