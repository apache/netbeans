
package test;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;
import javax.ejb.FinderException;


/**
 * This is the local-home interface for EntityA enterprise bean.
 */
public interface EntityALocalHome extends EJBLocalHome {
    
    EntityALocal findByPrimaryKey(String key)  throws FinderException;
    
    EntityALocal create(String key)  throws CreateException;
    
    
}
