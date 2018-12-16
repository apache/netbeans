package testPackage;

import javax.ejb.*;

/**
 * This is the bean class for the SalesRepDataTblBean enterprise bean.
 * Created Kdysi
 * @author Kdosi
 */
public abstract class SalesRepDataTblBean implements javax.ejb.EntityBean, testPackage.SalesRepDataTblLocalBusiness {
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
    
    
    public abstract java.io.Serializable getImage();
    public abstract void setImage(java.io.Serializable image);
    
    public abstract java.lang.String getResume();
    public abstract void setResume(java.lang.String resume);
    
    public abstract testPackage.SalesRepTblLocal getSalesRepNum();
    public abstract void setSalesRepNum(testPackage.SalesRepTblLocal salesRepNum);
    
    
    public java.lang.Object ejbCreate(java.io.Serializable image, java.lang.String resume, testPackage.SalesRepTblLocal salesRepNum)  throws javax.ejb.CreateException {
        if (salesRepNum == null) {
            throw new javax.ejb.CreateException("The field \"salesRepNum\" must not be null");
        }
        
        // TODO add additional validation code, throw CreateException if data is not valid
        setImage(image);
        setResume(resume);
        
        return null;
    }
    
    public void ejbPostCreate(java.io.Serializable image, java.lang.String resume, testPackage.SalesRepTblLocal salesRepNum) {
        // TODO populate relationships here if appropriate
        setSalesRepNum(salesRepNum);
        
    }
}
