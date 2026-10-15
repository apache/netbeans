/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB21/CmpRemote.java to edit this template
 */

package testGenerateJavaEE14;

import java.rmi.RemoteException;
import javax.ejb.EJBObject;

/**
 *
 * @author {user}
 */
public interface TestCmpRemote extends EJBObject {

    java.lang.Long getPk() throws RemoteException;
    
}
