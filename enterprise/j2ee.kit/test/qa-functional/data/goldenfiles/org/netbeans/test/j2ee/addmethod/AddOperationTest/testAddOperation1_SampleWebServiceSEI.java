package sample;

import java.rmi.Remote;


/**
 * This is the service endpoint interface for the SampleWebServiceweb service.
 * Created 2.6.2005 15:26:52
 * @author lm97939
 */

public interface SampleWebServiceSEI extends Remote {
    /**
     * Web service operation
     */
    public String operation1() throws java.rmi.RemoteException;
    
}
