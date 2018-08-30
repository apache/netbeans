
package testPackage;


/**
 * This is the local-home interface for OrderTbl enterprise bean.
 */
public interface OrderTblLocalHome extends javax.ejb.EJBLocalHome {
    
    testPackage.OrderTblLocal findByPrimaryKey(java.lang.Integer key)  throws javax.ejb.FinderException;

    public testPackage.OrderTblLocal create(java.lang.Integer orderNum, java.lang.Integer quantity, java.math.BigDecimal shippingCost, java.sql.Date salesDate, java.sql.Date shippingDate, java.sql.Timestamp deliveryDatetime, java.lang.String freightCompany, testPackage.CustomerTblLocal customerNum, testPackage.ProductTblLocal productNum, testPackage.SalesRepTblLocal repNum, testPackage.SalesTaxCodeTblLocal salesTaxStCd) throws javax.ejb.CreateException;
    
    
}
