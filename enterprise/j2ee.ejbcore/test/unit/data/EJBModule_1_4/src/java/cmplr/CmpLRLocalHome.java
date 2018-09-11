/*
 * CmpLRLocalHome.java
 *
 * Created on Feb 15, 2007, 4:52:32 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package cmplr;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;
import javax.ejb.FinderException;

/**
 *
 * @author klingo
 */
public interface CmpLRLocalHome extends EJBLocalHome {

    cmplr.CmpLRLocal findByPrimaryKey(java.lang.Long key)  throws FinderException;
    
    cmplr.CmpLRLocal create(java.lang.Long key)  throws CreateException;

}
