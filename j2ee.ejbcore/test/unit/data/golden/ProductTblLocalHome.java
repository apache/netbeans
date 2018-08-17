
package testPackage;


/**
 * This is the local-home interface for ProductTbl enterprise bean.
 */
public interface ProductTblLocalHome extends javax.ejb.EJBLocalHome {
    
    testPackage.ProductTblLocal findByPrimaryKey(java.lang.Integer key)  throws javax.ejb.FinderException;

    public testPackage.ProductTblLocal create(java.lang.Integer productNum, java.math.BigDecimal purchaseCost, java.lang.Integer qtyOnHand, java.math.BigDecimal markup, java.lang.Boolean avail, java.lang.String description, testPackage.ManufactureTblLocal mfrNum, testPackage.ProductCodeTblLocal productCode) throws javax.ejb.CreateException;
    
    
}
