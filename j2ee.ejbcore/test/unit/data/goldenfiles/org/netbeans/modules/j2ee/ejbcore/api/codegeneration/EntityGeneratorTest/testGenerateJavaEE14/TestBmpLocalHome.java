/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
