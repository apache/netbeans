/*
 * StatelessLRLocalHome.java
 *
 * Created on Feb 15, 2007, 4:02:30 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package statelesslr;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

/**
 *
 * @author klingo
 */
public interface StatelessLRLocalHome2 extends EJBLocalHome {
    
    statelesslr.StatelessLRLocal2 create()  throws CreateException;

}
