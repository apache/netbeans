
package testPackage;


/**
 * This is the local-home interface for ProductCodeTbl enterprise bean.
 */
public interface ProductCodeTblLocalHome extends javax.ejb.EJBLocalHome {
    
    testPackage.ProductCodeTblLocal findByPrimaryKey(java.lang.String key)  throws javax.ejb.FinderException;

    public testPackage.ProductCodeTblLocal create(java.lang.String prodCode, java.lang.String discountCode, java.lang.String description) throws javax.ejb.CreateException;
    
    
}
