package testPackage;

import javax.ejb.*;

/**
 * This is the bean class for the ProductCodeTblBean enterprise bean.
 * Created Kdysi
 * @author Kdosi
 */
public abstract class ProductCodeTblBean implements javax.ejb.EntityBean, testPackage.ProductCodeTblLocalBusiness {
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
    
    
    public abstract java.lang.String getProdCode();
    public abstract void setProdCode(java.lang.String prodCode);
    
    public abstract java.lang.String getDiscountCode();
    public abstract void setDiscountCode(java.lang.String discountCode);
    
    public abstract java.lang.String getDescription();
    public abstract void setDescription(java.lang.String description);
    
    public abstract java.util.Collection getProductTblBean();
    public abstract void setProductTblBean(java.util.Collection productTblBean);
    
    
    public java.lang.String ejbCreate(java.lang.String prodCode, java.lang.String discountCode, java.lang.String description)  throws javax.ejb.CreateException {
        if (prodCode == null) {
            throw new javax.ejb.CreateException("The field \"prodCode\" must not be null");
        }
        if (discountCode == null) {
            throw new javax.ejb.CreateException("The field \"discountCode\" must not be null");
        }
        
        // TODO add additional validation code, throw CreateException if data is not valid
        setProdCode(prodCode);
        setDiscountCode(discountCode);
        setDescription(description);
        
        return null;
    }
    
    public void ejbPostCreate(java.lang.String prodCode, java.lang.String discountCode, java.lang.String description) {
        // TODO populate relationships here if appropriate
        
    }
}
