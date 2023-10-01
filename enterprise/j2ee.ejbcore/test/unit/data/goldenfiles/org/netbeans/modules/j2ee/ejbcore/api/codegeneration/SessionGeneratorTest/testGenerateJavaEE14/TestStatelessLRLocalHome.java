/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB21/SessionLocalHome.java to edit this template
 */

package testGenerateJavaEE14;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

/**
 *
 * @author {user}
 */
public interface TestStatelessLRLocalHome extends EJBLocalHome {
    
    testGenerateJavaEE14.TestStatelessLRLocal create()  throws CreateException;

}
