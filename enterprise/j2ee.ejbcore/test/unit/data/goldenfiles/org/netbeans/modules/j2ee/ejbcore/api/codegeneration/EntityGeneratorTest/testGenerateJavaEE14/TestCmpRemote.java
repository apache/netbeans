/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
