
package testPackage;


/**
 * This is the local-home interface for ManufactureTbl enterprise bean.
 */
public interface ManufactureTblLocalHome extends javax.ejb.EJBLocalHome {
    
    testPackage.ManufactureTblLocal findByPrimaryKey(java.lang.Integer key)  throws javax.ejb.FinderException;

    public testPackage.ManufactureTblLocal create(java.lang.Integer mfrNum, java.lang.String name, java.lang.String addrLn1, java.lang.String addrLn2, java.lang.String city, java.lang.String state, java.lang.String zip, java.lang.String phone, java.lang.String fax, java.lang.String email, java.lang.String rep, java.lang.Boolean localCompany) throws javax.ejb.CreateException;
    
    
}
