
package testPackage;


/**
 * This is the local-home interface for OfficeTbl enterprise bean.
 */
public interface OfficeTblLocalHome extends javax.ejb.EJBLocalHome {
    
    testPackage.OfficeTblLocal findByPrimaryKey(java.lang.Integer key)  throws javax.ejb.FinderException;

    public testPackage.OfficeTblLocal create(java.lang.Integer officeNum, java.lang.String city, java.lang.String region, java.lang.Integer targetSales, java.sql.Date openDate, java.sql.Date closeDate, testPackage.OfficeTypeCodeTblLocal typeCode) throws javax.ejb.CreateException;
    
    
}
