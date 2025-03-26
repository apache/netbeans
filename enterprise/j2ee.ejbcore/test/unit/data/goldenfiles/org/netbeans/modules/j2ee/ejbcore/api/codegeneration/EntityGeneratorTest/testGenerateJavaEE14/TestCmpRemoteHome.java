/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB21/CmpRemoteHome.java to edit this template
 */

package testGenerateJavaEE14;

import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import javax.ejb.FinderException;

/**
 *
 * @author {user}
 */
public interface TestCmpRemoteHome extends EJBHome {
    
    testGenerateJavaEE14.TestCmpRemote findByPrimaryKey(java.lang.Long key)  throws FinderException, RemoteException;
    
    testGenerateJavaEE14.TestCmpRemote create(java.lang.Long key)  throws CreateException, RemoteException;

}
