package test;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

/**
 * This is the bean class for the TestingSessionBean enterprise bean.
 * Created 29.4.2005 15:24:25
 * @author lm97939
 */
public class TestingSessionBean implements javax.ejb.SessionBean, test.TestingSessionRemoteBusiness, test.TestingSessionLocalBusiness {
    private javax.ejb.SessionContext context;
    
    // <editor-fold defaultstate="collapsed" desc="EJB infrastructure methods. Click the + sign on the left to edit the code.">
    // TODO Add code to acquire and use other enterprise resources (DataSource, JMS, enterprise bean, Web services)
    // TODO Add business methods or web service operations
    /**
     * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
     */
    public void setSessionContext(javax.ejb.SessionContext aContext) {
        context = aContext;
    }
    
    /**
     * @see javax.ejb.SessionBean#ejbActivate()
     */
    public void ejbActivate() {
        
    }
    
    /**
     * @see javax.ejb.SessionBean#ejbPassivate()
     */
    public void ejbPassivate() {
        
    }
    
    /**
     * @see javax.ejb.SessionBean#ejbRemove()
     */
    public void ejbRemove() {
        
    }
    // </editor-fold>
    
    /**
     * See section 7.10.3 of the EJB 2.0 specification
     * See section 7.11.3 of the EJB 2.1 specification
     */
    public void ejbCreate() {
        // TODO implement ejbCreate if necessary, acquire resources
        // This method has access to the JNDI context so resource aquisition
        // spanning all methods can be performed here such as home interfaces
        // and data sources.
    }

    public String testBusinessMethod1() {
        return null;
    }

    public String testBusinessMethod2(String a, int b) throws Exception {
        return null;
    }

    private TestingEntityLocalHome lookupTestingEntityBean() {
        try {
            Context c = new InitialContext();
            TestingEntityLocalHome rv = (TestingEntityLocalHome) c.lookup("java:comp/env/TestingEntityBean");
            return rv;
        } catch (NamingException ne) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", ne);
            throw new RuntimeException(ne);
        }
    }

    private TestingEntityRemoteHome lookupMyTestingEntityBean() {
        try {
            Context c = new InitialContext();
            Object remote = c.lookup("java:comp/env/ejb/MyTestingEntityBean");
            TestingEntityRemoteHome rv = (TestingEntityRemoteHome) PortableRemoteObject.narrow(remote, TestingEntityRemoteHome.class);
            return rv;
        } catch (NamingException ne) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", ne);
            throw new RuntimeException(ne);
        }
    }
    
    
    
    // Add business logic below. (Right-click in editor and choose
    // "EJB Methods > Add Business Method" or "Web Service > Add Operation")
    
    
    
}
