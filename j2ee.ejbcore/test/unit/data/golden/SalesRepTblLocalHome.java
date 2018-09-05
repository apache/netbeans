
package testPackage;


/**
 * This is the local-home interface for SalesRepTbl enterprise bean.
 */
public interface SalesRepTblLocalHome extends javax.ejb.EJBLocalHome {
    
    testPackage.SalesRepTblLocal findByPrimaryKey(java.lang.Integer key)  throws javax.ejb.FinderException;

    public testPackage.SalesRepTblLocal create(java.lang.Integer salesRepNum, java.lang.String firstName, java.lang.String lastName, java.lang.Integer quota, java.math.BigDecimal ytdSales, java.math.BigDecimal lastYrSales, java.math.BigDecimal commissionRate, java.lang.Integer travelAllow, java.sql.Date hireDate, java.lang.Float twoYrSalesGoal, testPackage.OfficeTblLocal officeNum) throws javax.ejb.CreateException;
    
    
}
