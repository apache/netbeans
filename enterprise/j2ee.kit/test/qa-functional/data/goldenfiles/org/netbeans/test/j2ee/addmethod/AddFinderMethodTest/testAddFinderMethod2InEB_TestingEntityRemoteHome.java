
package test;

import java.io.IOException;
import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.FinderException;


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

    TestingEntityRemote createTest2(String a, int b) throws CreateException, IOException, RemoteException;

    String homeTestMethod1() throws RemoteException;

    TestingEntityRemote findByTest3(String a) throws FinderException, RemoteException;
    
    
}
