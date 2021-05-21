package testPackage;

import javax.ejb.*;

/**
 * This is the bean class for the OfficeTypeCodeTblBean enterprise bean.
 * Created Kdysi
 * @author Kdosi
 */
public abstract class OfficeTypeCodeTblBean implements javax.ejb.EntityBean, testPackage.OfficeTypeCodeTblLocalBusiness {
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
    
    
    public abstract java.lang.String getTypeCode();
    public abstract void setTypeCode(java.lang.String typeCode);
    
    public abstract java.lang.String getDescription();
    public abstract void setDescription(java.lang.String description);
    
    public abstract java.lang.String getMisc();
    public abstract void setMisc(java.lang.String misc);
    
    public abstract java.util.Collection getOfficeTblBean();
    public abstract void setOfficeTblBean(java.util.Collection officeTblBean);
    
    
    public java.lang.String ejbCreate(java.lang.String typeCode, java.lang.String description, java.lang.String misc)  throws javax.ejb.CreateException {
        if (typeCode == null) {
            throw new javax.ejb.CreateException("The field \"typeCode\" must not be null");
        }
        
        // TODO add additional validation code, throw CreateException if data is not valid
        setTypeCode(typeCode);
        setDescription(description);
        setMisc(misc);
        
        return null;
    }
    
    public void ejbPostCreate(java.lang.String typeCode, java.lang.String description, java.lang.String misc) {
        // TODO populate relationships here if appropriate
        
    }
}
