
package testPackage;


/**
 * This is the local-home interface for CustomerTbl enterprise bean.
 */
public interface CustomerTblLocalHome extends javax.ejb.EJBLocalHome {
    
    testPackage.CustomerTblLocal findByPrimaryKey(java.lang.Integer key)  throws javax.ejb.FinderException;

    public testPackage.CustomerTblLocal create(java.lang.Integer customerNum, java.lang.String name, java.lang.String addrLn1, java.lang.String addrLn2, java.lang.String city, java.lang.String state, java.lang.String phone, java.lang.String fax, java.lang.String email, java.lang.Integer creditLimit, java.sql.Date lastSaleDate, java.sql.Time lastSaleTime, testPackage.DiscountCodeTblLocal discountCode, testPackage.MicroMarketsTblLocal zip) throws javax.ejb.CreateException;
    
    
}
