
package testPackage;


/**
 * This is the local-home interface for MicroMarketsTbl enterprise bean.
 */
public interface MicroMarketsTblLocalHome extends javax.ejb.EJBLocalHome {
    
    testPackage.MicroMarketsTblLocal findByPrimaryKey(java.lang.String key)  throws javax.ejb.FinderException;

    public testPackage.MicroMarketsTblLocal create(java.lang.String zipCode, java.lang.Double radius, java.lang.Double areaLength, java.lang.Double areaWidth) throws javax.ejb.CreateException;
    
    
}
