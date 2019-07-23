/*
 * StatelessLRRemoteHome.java
 *
 * Created on Feb 15, 2007, 4:02:30 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package statelesslr;

import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;

/**
 *
 * @author klingo
 */
public interface StatelessLRRemoteHome2 extends EJBHome {

    statelesslr.StatelessLRRemote2 create()  throws CreateException, RemoteException;
    
}
