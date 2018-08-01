
package cmp;


/**
 * This is the local-home interface for Customer enterprise bean.
 */
public interface CustomerLocalHome extends javax.ejb.EJBLocalHome {
    
    
    cmp.CustomerLocal findByPrimaryKey(java.lang.Long key)  throws javax.ejb.FinderException;

    public cmp.CustomerLocal create(java.lang.Long id, java.lang.String lastName, java.lang.String firstName) throws javax.ejb.CreateException;

    java.util.Collection findById(java.lang.Long id) throws javax.ejb.FinderException;

    java.util.Collection findByLastName(java.lang.String lastName) throws javax.ejb.FinderException;

    java.util.Collection findByFirstName(java.lang.String firstName) throws javax.ejb.FinderException;
    
    
}
