package testPackage;

import javax.ejb.*;

/**
 * This is the bean class for the OrderTblBean enterprise bean.
 * Created Kdysi
 * @author Kdosi
 */
public abstract class OrderTblBean implements javax.ejb.EntityBean, testPackage.OrderTblLocalBusiness {
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
    
    
    public abstract java.lang.Integer getOrderNum();
    public abstract void setOrderNum(java.lang.Integer orderNum);
    
    public abstract java.lang.Integer getQuantity();
    public abstract void setQuantity(java.lang.Integer quantity);
    
    public abstract java.math.BigDecimal getShippingCost();
    public abstract void setShippingCost(java.math.BigDecimal shippingCost);
    
    public abstract java.sql.Date getSalesDate();
    public abstract void setSalesDate(java.sql.Date salesDate);
    
    public abstract java.sql.Date getShippingDate();
    public abstract void setShippingDate(java.sql.Date shippingDate);
    
    public abstract java.sql.Timestamp getDeliveryDatetime();
    public abstract void setDeliveryDatetime(java.sql.Timestamp deliveryDatetime);
    
    public abstract java.lang.String getFreightCompany();
    public abstract void setFreightCompany(java.lang.String freightCompany);
    
    public abstract testPackage.CustomerTblLocal getCustomerNum();
    public abstract void setCustomerNum(testPackage.CustomerTblLocal customerNum);
    
    public abstract testPackage.ProductTblLocal getProductNum();
    public abstract void setProductNum(testPackage.ProductTblLocal productNum);
    
    public abstract testPackage.SalesRepTblLocal getRepNum();
    public abstract void setRepNum(testPackage.SalesRepTblLocal repNum);
    
    public abstract testPackage.SalesTaxCodeTblLocal getSalesTaxStCd();
    public abstract void setSalesTaxStCd(testPackage.SalesTaxCodeTblLocal salesTaxStCd);
    
    
    public java.lang.Integer ejbCreate(java.lang.Integer orderNum, java.lang.Integer quantity, java.math.BigDecimal shippingCost, java.sql.Date salesDate, java.sql.Date shippingDate, java.sql.Timestamp deliveryDatetime, java.lang.String freightCompany, testPackage.CustomerTblLocal customerNum, testPackage.ProductTblLocal productNum, testPackage.SalesRepTblLocal repNum, testPackage.SalesTaxCodeTblLocal salesTaxStCd)  throws javax.ejb.CreateException {
        if (orderNum == null) {
            throw new javax.ejb.CreateException("The field \"orderNum\" must not be null");
        }
        if (customerNum == null) {
            throw new javax.ejb.CreateException("The field \"customerNum\" must not be null");
        }
        if (productNum == null) {
            throw new javax.ejb.CreateException("The field \"productNum\" must not be null");
        }
        if (repNum == null) {
            throw new javax.ejb.CreateException("The field \"repNum\" must not be null");
        }
        if (salesTaxStCd == null) {
            throw new javax.ejb.CreateException("The field \"salesTaxStCd\" must not be null");
        }
        
        // TODO add additional validation code, throw CreateException if data is not valid
        setOrderNum(orderNum);
        setQuantity(quantity);
        setShippingCost(shippingCost);
        setSalesDate(salesDate);
        setShippingDate(shippingDate);
        setDeliveryDatetime(deliveryDatetime);
        setFreightCompany(freightCompany);
        
        return null;
    }
    
    public void ejbPostCreate(java.lang.Integer orderNum, java.lang.Integer quantity, java.math.BigDecimal shippingCost, java.sql.Date salesDate, java.sql.Date shippingDate, java.sql.Timestamp deliveryDatetime, java.lang.String freightCompany, testPackage.CustomerTblLocal customerNum, testPackage.ProductTblLocal productNum, testPackage.SalesRepTblLocal repNum, testPackage.SalesTaxCodeTblLocal salesTaxStCd) {
        // TODO populate relationships here if appropriate
        setCustomerNum(customerNum);
        setProductNum(productNum);
        setRepNum(repNum);
        setSalesTaxStCd(salesTaxStCd);
        
    }
}
