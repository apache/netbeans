
package test;

import java.rmi.RemoteException;


/**
 * This is the remote interface for TestingEntity enterprise bean.
 */
public interface TestingEntityRemote extends javax.ejb.EJBObject, test.TestingEntityRemoteBusiness {

    String testBusinessMethod1() throws RemoteException;

    int getCmpTestField2x();

    void setCmpTestField2x(int cmpTestField2x);
    
    
}
