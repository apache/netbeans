
package testPackage;


/**
 * This is the local-home interface for DiscountCodeTbl enterprise bean.
 */
public interface DiscountCodeTblLocalHome extends javax.ejb.EJBLocalHome {
    
    testPackage.DiscountCodeTblLocal findByPrimaryKey(java.lang.String key)  throws javax.ejb.FinderException;

    public testPackage.DiscountCodeTblLocal create(java.lang.String discountCode, java.math.BigDecimal rate) throws javax.ejb.CreateException;
    
    
}
