/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB21/BmpLocalHome.java to edit this template
 */

package testGenerateJavaEE14;

import javax.ejb.EJBLocalHome;
import javax.ejb.FinderException;

/**
 *
 * @author {user}
 */
public interface TestBmpLocalHome extends EJBLocalHome {

    testGenerateJavaEE14.TestBmpLocal findByPrimaryKey(java.lang.Long key) throws FinderException;
    
}
