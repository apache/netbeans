/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB21/SessionRemoteHome.java to edit this template
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
