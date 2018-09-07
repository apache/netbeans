
package testPackage;


/**
 * This is the local-home interface for SalesRepDataTbl enterprise bean.
 */
public interface SalesRepDataTblLocalHome extends javax.ejb.EJBLocalHome {
    
    testPackage.SalesRepDataTblLocal findByPrimaryKey(java.lang.Object key)  throws javax.ejb.FinderException;

    public testPackage.SalesRepDataTblLocal create(java.io.Serializable image, java.lang.String resume, testPackage.SalesRepTblLocal salesRepNum) throws javax.ejb.CreateException;
    
    
}
