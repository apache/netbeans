
package test;


/**
 * This is the home interface for TestingSession enterprise bean.
 */
public interface TestingSessionRemoteHome extends javax.ejb.EJBHome {
    
    
    
    /**
     *
     */
    test.TestingSessionRemote create()  throws javax.ejb.CreateException, java.rmi.RemoteException;
    
    
}
