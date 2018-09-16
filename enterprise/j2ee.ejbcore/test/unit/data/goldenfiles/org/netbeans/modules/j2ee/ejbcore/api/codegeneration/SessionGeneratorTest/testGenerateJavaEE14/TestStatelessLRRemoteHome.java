/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package testGenerateJavaEE14;

import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;

/**
 *
 * @author {user}
 */
public interface TestStatelessLRRemoteHome extends EJBHome {

    testGenerateJavaEE14.TestStatelessLRRemote create()  throws CreateException, RemoteException;
    
}
