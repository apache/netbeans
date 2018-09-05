/*
 * CmpLRBean.java
 *
 * Created on Feb 15, 2007, 4:52:32 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package cmplr;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;

/**
 *
 * @author klingo
 */
public abstract class CmpLRBean implements EntityBean {

    private EntityContext context;
    
    // <editor-fold defaultstate="collapsed" desc="EJB infrastructure methods. Click on the + sign on the left to edit the code.">

    // TODO Consider creating Transfer Object to encapsulate data
    // TODO Review finder methods

    /**
     * @see javax.ejb.EntityBean#setEntityContext(javax.ejb.EntityContext)
     */
    public void setEntityContext(EntityContext aContext) {
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
    
    public abstract java.lang.Long getKey();

    public abstract void setKey(java.lang.Long key);
    
    public java.lang.Long ejbCreate(java.lang.Long key)  throws CreateException {
        if (key == null) {
            throw new CreateException("The field \"key\" must not be null");
        }
        
        // TODO add additional validation code, throw CreateException if data is not valid
        setKey(key);
        
        return null;
    }
    
    public void ejbPostCreate(java.lang.Long key) {
        // TODO populate relationships here if appropriate
    }

}
