package testPackage;

import javax.ejb.*;

/**
 * This is the bean class for the SalesTaxCodeTblBean enterprise bean.
 * Created Kdysi
 * @author Kdosi
 */
public abstract class SalesTaxCodeTblBean implements javax.ejb.EntityBean, testPackage.SalesTaxCodeTblLocalBusiness {
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
    
    
    public abstract java.lang.String getStateCode();
    public abstract void setStateCode(java.lang.String stateCode);
    
    public abstract java.sql.Date getEffectDate();
    public abstract void setEffectDate(java.sql.Date effectDate);
    
    public abstract java.math.BigDecimal getRate();
    public abstract void setRate(java.math.BigDecimal rate);
    
    public abstract java.util.Collection getOrderTblBean();
    public abstract void setOrderTblBean(java.util.Collection orderTblBean);
    
    
    public java.lang.String ejbCreate(java.lang.String stateCode, java.sql.Date effectDate, java.math.BigDecimal rate)  throws javax.ejb.CreateException {
        if (stateCode == null) {
            throw new javax.ejb.CreateException("The field \"stateCode\" must not be null");
        }
        if (effectDate == null) {
            throw new javax.ejb.CreateException("The field \"effectDate\" must not be null");
        }
        
        // TODO add additional validation code, throw CreateException if data is not valid
        setStateCode(stateCode);
        setEffectDate(effectDate);
        setRate(rate);
        
        return null;
    }
    
    public void ejbPostCreate(java.lang.String stateCode, java.sql.Date effectDate, java.math.BigDecimal rate) {
        // TODO populate relationships here if appropriate
        
    }
}
