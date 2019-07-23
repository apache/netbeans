
package test;

import java.rmi.RemoteException;


/**
 * This is the remote interface for TestingSession enterprise bean.
 */
public interface TestingSessionRemote extends javax.ejb.EJBObject, test.TestingSessionRemoteBusiness {

    String testBusinessMethod1() throws RemoteException;
    
    
}
