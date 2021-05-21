package cmp;

import javax.ejb.*;

/**
 * This is the bean class for the CustomerBean enterprise bean.
 */
public abstract class CustomerBean implements javax.ejb.EntityBean, cmp.CustomerLocalBusiness {
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
    
    
    public abstract java.lang.Long getId();
    public abstract void setId(java.lang.Long id);
    
    public abstract java.lang.String getLastName();
    public abstract void setLastName(java.lang.String lastName);
    
    public abstract java.lang.String getFirstName();
    public abstract void setFirstName(java.lang.String firstName);
    
    
    public java.lang.Long ejbCreate(java.lang.Long id, java.lang.String lastName, java.lang.String firstName)  throws javax.ejb.CreateException {
        if (id == null) {
            throw new javax.ejb.CreateException("The field \"id\" must not be null");
        }
        if (lastName == null) {
            throw new javax.ejb.CreateException("The field \"lastName\" must not be null");
        }
        
        // TODO add additional validation code, throw CreateException if data is not valid
        setId(id);
        setLastName(lastName);
        setFirstName(firstName);
        
        return null;
    }
    
    public void ejbPostCreate(java.lang.Long id, java.lang.String lastName, java.lang.String firstName) {
        // TODO populate relationships here if appropriate
        
    }
}
