package testPackage;

import javax.ejb.*;

/**
 * This is the bean class for the ProductTblBean enterprise bean.
 * Created Kdysi
 * @author Kdosi
 */
public abstract class ProductTblBean implements javax.ejb.EntityBean, testPackage.ProductTblLocalBusiness {
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
    
    
    public abstract java.lang.Integer getProductNum();
    public abstract void setProductNum(java.lang.Integer productNum);
    
    public abstract java.math.BigDecimal getPurchaseCost();
    public abstract void setPurchaseCost(java.math.BigDecimal purchaseCost);
    
    public abstract java.lang.Integer getQtyOnHand();
    public abstract void setQtyOnHand(java.lang.Integer qtyOnHand);
    
    public abstract java.math.BigDecimal getMarkup();
    public abstract void setMarkup(java.math.BigDecimal markup);
    
    public abstract java.lang.Boolean getAvail();
    public abstract void setAvail(java.lang.Boolean avail);
    
    public abstract java.lang.String getDescription();
    public abstract void setDescription(java.lang.String description);
    
    public abstract java.util.Collection getOrderTblBean();
    public abstract void setOrderTblBean(java.util.Collection orderTblBean);
    
    public abstract testPackage.ManufactureTblLocal getMfrNum();
    public abstract void setMfrNum(testPackage.ManufactureTblLocal mfrNum);
    
    public abstract testPackage.ProductCodeTblLocal getProductCode();
    public abstract void setProductCode(testPackage.ProductCodeTblLocal productCode);
    
    
    public java.lang.Integer ejbCreate(java.lang.Integer productNum, java.math.BigDecimal purchaseCost, java.lang.Integer qtyOnHand, java.math.BigDecimal markup, java.lang.Boolean avail, java.lang.String description, testPackage.ManufactureTblLocal mfrNum, testPackage.ProductCodeTblLocal productCode)  throws javax.ejb.CreateException {
        if (productNum == null) {
            throw new javax.ejb.CreateException("The field \"productNum\" must not be null");
        }
        if (mfrNum == null) {
            throw new javax.ejb.CreateException("The field \"mfrNum\" must not be null");
        }
        if (productCode == null) {
            throw new javax.ejb.CreateException("The field \"productCode\" must not be null");
        }
        
        // TODO add additional validation code, throw CreateException if data is not valid
        setProductNum(productNum);
        setPurchaseCost(purchaseCost);
        setQtyOnHand(qtyOnHand);
        setMarkup(markup);
        setAvail(avail);
        setDescription(description);
        
        return null;
    }
    
    public void ejbPostCreate(java.lang.Integer productNum, java.math.BigDecimal purchaseCost, java.lang.Integer qtyOnHand, java.math.BigDecimal markup, java.lang.Boolean avail, java.lang.String description, testPackage.ManufactureTblLocal mfrNum, testPackage.ProductCodeTblLocal productCode) {
        // TODO populate relationships here if appropriate
        setMfrNum(mfrNum);
        setProductCode(productCode);
        
    }
}
