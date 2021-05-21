package test;

import java.io.IOException;
import javax.ejb.*;

/**
 * This is the bean class for the TestingEntityBean enterprise bean.
 * Created 6.5.2005 15:11:14
 * @author lm97939
 */
public abstract class TestingEntityBean implements javax.ejb.EntityBean, test.TestingEntityRemoteBusiness, test.TestingEntityLocalBusiness {
    private javax.ejb.EntityContext context;
    
    // <editor-fold defaultstate="collapsed" desc="EJB infrastructure methods. Click on the + sign on the left to edit the code.">
    // TODO Consider creating Transfer Object to encapsulate data
    // TODO Review finder methods
    /**
     * @see javax.ejb.EntityBean#setEntityContext(javax.ejb.EntityContext)
     */
    public void setEntityContext(javax.ejb.EntityContext aContext) {
        context = aContext;
    }
    
    /**
     * @see javax.ejb.EntityBean#ejbActivate()
     */
    public void ejbActivate() {
        
    }
    
    /**
     * @see javax.ejb.EntityBean#ejbPassivate()
     */
    public void ejbPassivate() {
        
    }
    
    /**
     * @see javax.ejb.EntityBean#ejbRemove()
     */
    public void ejbRemove() {
        
    }
    
    /**
     * @see javax.ejb.EntityBean#unsetEntityContext()
     */
    public void unsetEntityContext() {
        context = null;
    }
    
    /**
     * @see javax.ejb.EntityBean#ejbLoad()
     */
    public void ejbLoad() {
        
    }
    
    /**
     * @see javax.ejb.EntityBean#ejbStore()
     */
    public void ejbStore() {
        
    }
    // </editor-fold>
    
    
    public abstract java.lang.String getKey();
    public abstract void setKey(java.lang.String key);
    
    
    public java.lang.String ejbCreate(java.lang.String key)  throws javax.ejb.CreateException {
        if (key == null) {
            throw new javax.ejb.CreateException("The field \"key\" must not be null");
        }
        
        // TODO add additional validation code, throw CreateException if data is not valid
        setKey(key);
        
        return null;
    }
    
    public void ejbPostCreate(java.lang.String key) {
        // TODO populate relationships here if appropriate
        
    }

    public String testBusinessMethod1() {
        return null;
    }

    public String testBusinessMethod2(String a, boolean b) throws Exception {
        return null;
    }

    public String ejbCreateTest1() throws CreateException {
        return null;
    }

    public void ejbPostCreateTest1() throws CreateException {
    }

    public String ejbCreateTest2(String a, int b) throws CreateException, IOException {
        return null;
    }

    public void ejbPostCreateTest2(String a, int b) throws CreateException, IOException {
    }

    public String ejbHomeHomeTestMethod1() {
        return null;
    }
}
