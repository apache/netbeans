/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB21/CmpLocalHome.java to edit this template
 */

package testGenerateJavaEE14;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;
import javax.ejb.FinderException;

/**
 *
 * @author {user}
 */
public interface TestCmpLocalHome extends EJBLocalHome {

    testGenerateJavaEE14.TestCmpLocal findByPrimaryKey(java.lang.Long key)  throws FinderException;
    
    testGenerateJavaEE14.TestCmpLocal create(java.lang.Long key)  throws CreateException;

}
