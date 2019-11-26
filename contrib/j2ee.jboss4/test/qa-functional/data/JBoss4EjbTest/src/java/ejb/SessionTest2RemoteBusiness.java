
package ejb;


/**
 * This is the business interface for SessionTest2 enterprise bean.
 */
public interface SessionTest2RemoteBusiness {
    String greetings(String who) throws java.rmi.RemoteException;
    
}
