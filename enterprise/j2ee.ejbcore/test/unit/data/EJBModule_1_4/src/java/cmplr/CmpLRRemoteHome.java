/*
 * CmpLRRemoteHome.java
 *
 * Created on Feb 15, 2007, 4:52:32 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package cmplr;

import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import javax.ejb.FinderException;

/**
 *
 * @author klingo
 */
public interface CmpLRRemoteHome extends EJBHome {
    
    cmplr.CmpLRRemote findByPrimaryKey(java.lang.Long key)  throws FinderException, RemoteException;
    
    cmplr.CmpLRRemote create(java.lang.Long key)  throws CreateException, RemoteException;

}
