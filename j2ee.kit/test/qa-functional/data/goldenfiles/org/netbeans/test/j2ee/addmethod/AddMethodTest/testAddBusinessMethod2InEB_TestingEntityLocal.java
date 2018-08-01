
package test;


/**
 * This is the local interface for TestingEntity enterprise bean.
 */
public interface TestingEntityLocal extends javax.ejb.EJBLocalObject, test.TestingEntityLocalBusiness {

    String testBusinessMethod1();

    String testBusinessMethod2(String a, boolean b) throws Exception;
    
    
}
