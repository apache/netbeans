
package test;


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
    
    
}
