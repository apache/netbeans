/*
 * StatefulLRRemoteHome.java
 *
 * Created on Feb 15, 2007, 4:02:47 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package statefullr;

import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;

/**
 *
 * @author klingo
 */
public interface StatefulLRRemoteHome extends EJBHome {

    statefullr.StatefulLRRemote create()  throws CreateException, RemoteException;
    
}
