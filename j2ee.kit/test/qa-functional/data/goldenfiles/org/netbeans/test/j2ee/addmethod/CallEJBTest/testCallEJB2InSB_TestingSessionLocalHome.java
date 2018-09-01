
package test;


/**
 * This is the local-home interface for TestingSession enterprise bean.
 */
public interface TestingSessionLocalHome extends javax.ejb.EJBLocalHome {
    
    
    
    /**
     *
     */
    test.TestingSessionLocal create()  throws javax.ejb.CreateException;
    
    
}
