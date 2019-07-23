
package testPackage;


/**
 * This is the local-home interface for OfficeTypeCodeTbl enterprise bean.
 */
public interface OfficeTypeCodeTblLocalHome extends javax.ejb.EJBLocalHome {
    
    testPackage.OfficeTypeCodeTblLocal findByPrimaryKey(java.lang.String key)  throws javax.ejb.FinderException;

    public testPackage.OfficeTypeCodeTblLocal create(java.lang.String typeCode, java.lang.String description, java.lang.String misc) throws javax.ejb.CreateException;
    
    
}
