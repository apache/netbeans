package sample;

import java.rmi.RemoteException;


/**
 * This is the implementation bean class for the SampleWebService web service.
 * Created 2.6.2005 15:26:52
 * @author lm97939
 */
public class SampleWebServiceImpl implements SampleWebServiceSEI {
    
    
    // Enter web service operations here. (Popup menu: Web Service->Add Operation)
    /**
     * Web service operation
     */
    public String operation1() throws RemoteException {
        // TODO implement operation 
return lookupSampleSessionBean().sampleBusinessMethod();
    }

    /**
     * Web service operation
     */
    public String operation2(String a, int b) throws java.lang.Exception, java.rmi.RemoteException {
        // TODO implement operation 
        return null;
    }

    private sample.SampleSessionRemote lookupSampleSessionBean() {
        try {
            javax.naming.Context c = new javax.naming.InitialContext();
            Object remote = c.lookup("java:comp/env/ejb/SampleSessionBean");
            sample.SampleSessionRemoteHome rv = (sample.SampleSessionRemoteHome) javax.rmi.PortableRemoteObject.narrow(remote, sample.SampleSessionRemoteHome.class);
            return rv.create();
        }
        catch(javax.naming.NamingException ne) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE,"exception caught" ,ne);
            throw new RuntimeException(ne);
        }
        catch(javax.ejb.CreateException ce) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE,"exception caught" ,ce);
            throw new RuntimeException(ce);
        }
        catch(java.rmi.RemoteException re) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE,"exception caught" ,re);
            throw new RuntimeException(re);
        }
    }
}
