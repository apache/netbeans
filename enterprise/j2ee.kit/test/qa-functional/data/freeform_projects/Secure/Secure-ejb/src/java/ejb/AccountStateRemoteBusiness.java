
package ejb;


/**
 * This is the business interface for AccountState enterprise bean.
 */
public interface AccountStateRemoteBusiness {
    String getStatus(java.lang.String user) throws java.rmi.RemoteException;
    
}
