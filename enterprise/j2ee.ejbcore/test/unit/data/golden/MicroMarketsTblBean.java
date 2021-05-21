package testPackage;

import javax.ejb.*;

/**
 * This is the bean class for the MicroMarketsTblBean enterprise bean.
 * Created Kdysi
 * @author Kdosi
 */
public abstract class MicroMarketsTblBean implements javax.ejb.EntityBean, testPackage.MicroMarketsTblLocalBusiness {
    private javax.ejb.EntityContext context;
    
    // <editor-fold defaultstate="collapsed" desc="EJB infrastructure methods. Click on the + sign on the left to edit the code.">
    // TODO Consider creating Transfer Object to encapsulate data
    // TODO Review finder methods
    /**
     * @see javax.ejb.EntityBean#setEntityContext(javax.ejb.EntityContext)
     */
    public void setEntityContext(javax.ejb.EntityContext aContext) {
        context = aContext;
    }
    
    /**
     * @see javax.ejb.EntityBean#ejbActivate()
     */
    public void ejbActivate() {
        
    }
    
    /**
     * @see javax.ejb.EntityBean#ejbPassivate()
     */
    public void ejbPassivate() {
        
    }
    
    /**
     * @see javax.ejb.EntityBean#ejbRemove()
     */
    public void ejbRemove() {
        
    }
    
    /**
     * @see javax.ejb.EntityBean#unsetEntityContext()
     */
    public void unsetEntityContext() {
        context = null;
    }
    
    /**
     * @see javax.ejb.EntityBean#ejbLoad()
     */
    public void ejbLoad() {
        
    }
    
    /**
     * @see javax.ejb.EntityBean#ejbStore()
     */
    public void ejbStore() {
        
    }
    // </editor-fold>
    
    
    public abstract java.lang.String getZipCode();
    public abstract void setZipCode(java.lang.String zipCode);
    
    public abstract java.lang.Double getRadius();
    public abstract void setRadius(java.lang.Double radius);
    
    public abstract java.lang.Double getAreaLength();
    public abstract void setAreaLength(java.lang.Double areaLength);
    
    public abstract java.lang.Double getAreaWidth();
    public abstract void setAreaWidth(java.lang.Double areaWidth);
    
    public abstract java.util.Collection getCustomerTblBean();
    public abstract void setCustomerTblBean(java.util.Collection customerTblBean);
    
    
    public java.lang.String ejbCreate(java.lang.String zipCode, java.lang.Double radius, java.lang.Double areaLength, java.lang.Double areaWidth)  throws javax.ejb.CreateException {
        if (zipCode == null) {
            throw new javax.ejb.CreateException("The field \"zipCode\" must not be null");
        }
        
        // TODO add additional validation code, throw CreateException if data is not valid
        setZipCode(zipCode);
        setRadius(radius);
        setAreaLength(areaLength);
        setAreaWidth(areaWidth);
        
        return null;
    }
    
    public void ejbPostCreate(java.lang.String zipCode, java.lang.Double radius, java.lang.Double areaLength, java.lang.Double areaWidth) {
        // TODO populate relationships here if appropriate
        
    }
}
