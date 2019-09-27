package cmp;

/**
 * This is the remote home interface for CustomerRemoteHome enterprise bean
 */
public interface CustomerRemoteHome extends javax.ejb.EJBHome {

    cmp.CustomerRemote findByPrimaryKey(java.lang.String key) throws javax.ejb.FinderException, java.rmi.RemoteException;

    cmp.CustomerRemote create(java.lang.Long id, java.lang.String lastName, java.lang.String firstName) throws javax.ejb.CreateException, java.rmi.RemoteException;
}
