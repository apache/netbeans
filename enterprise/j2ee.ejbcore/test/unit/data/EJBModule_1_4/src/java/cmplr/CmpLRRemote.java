/*
 * CmpLRRemote.java
 *
 * Created on Feb 15, 2007, 4:52:32 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package cmplr;

import java.rmi.RemoteException;
import javax.ejb.EJBObject;

/**
 *
 * @author klingo
 */
public interface CmpLRRemote extends EJBObject {

    java.lang.Long getKey() throws RemoteException;
    
}
