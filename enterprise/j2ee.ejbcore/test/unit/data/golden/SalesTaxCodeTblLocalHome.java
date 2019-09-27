
package testPackage;


/**
 * This is the local-home interface for SalesTaxCodeTbl enterprise bean.
 */
public interface SalesTaxCodeTblLocalHome extends javax.ejb.EJBLocalHome {
    
    testPackage.SalesTaxCodeTblLocal findByPrimaryKey(java.lang.String key)  throws javax.ejb.FinderException;

    public testPackage.SalesTaxCodeTblLocal create(java.lang.String stateCode, java.sql.Date effectDate, java.math.BigDecimal rate) throws javax.ejb.CreateException;
    
    
}
