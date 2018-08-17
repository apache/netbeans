
package test;


/**
 * This is the home interface for TestingEntity enterprise bean.
 */
public interface TestingEntityRemoteHome extends javax.ejb.EJBHome {
    
    
    
    /**
     *
     */
    test.TestingEntityRemote findByPrimaryKey(java.lang.String key)  throws javax.ejb.FinderException, java.rmi.RemoteException;
    
    
    
    /**
     *
     */
    test.TestingEntityRemote create(java.lang.String key)  throws javax.ejb.CreateException, java.rmi.RemoteException;
    
    
}
