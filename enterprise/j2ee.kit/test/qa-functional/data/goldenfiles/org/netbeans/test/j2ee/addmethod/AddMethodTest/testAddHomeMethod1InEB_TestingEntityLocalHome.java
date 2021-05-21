
package test;

import java.io.IOException;
import javax.ejb.CreateException;


/**
 * This is the local-home interface for TestingEntity enterprise bean.
 */
public interface TestingEntityLocalHome extends javax.ejb.EJBLocalHome {
    
    
    
    /**
     *
     */
    test.TestingEntityLocal findByPrimaryKey(java.lang.String key)  throws javax.ejb.FinderException;
    
    
    
    /**
     *
     */
    test.TestingEntityLocal create(java.lang.String key)  throws javax.ejb.CreateException;

    TestingEntityLocal createTest1() throws CreateException;

    TestingEntityLocal createTest2(String a, int b) throws CreateException, IOException;

    String homeTestMethod1();
    
    
}
