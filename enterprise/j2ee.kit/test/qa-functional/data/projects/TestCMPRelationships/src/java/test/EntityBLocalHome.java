
package test;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;
import javax.ejb.FinderException;


/**
 * This is the local-home interface for EntityB enterprise bean.
 */
public interface EntityBLocalHome extends EJBLocalHome {
    
    EntityBLocal findByPrimaryKey(String key)  throws FinderException;
    
    EntityBLocal create(String key)  throws CreateException;
    
    
}
