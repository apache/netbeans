
package ejb;


/**
 * This is the local-home interface for AccountState enterprise bean.
 */
public interface AccountStateLocalHome extends javax.ejb.EJBLocalHome {
    
    
    
    /**
     *
     */
    ejb.AccountStateLocal create()  throws javax.ejb.CreateException;
    
    
}
