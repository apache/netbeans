package testPackage;

import javax.ejb.*;

/**
 * This is the bean class for the CustomerTblBean enterprise bean.
 * Created Kdysi
 * @author Kdosi
 */
public abstract class CustomerTblBean implements javax.ejb.EntityBean, testPackage.CustomerTblLocalBusiness {
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
    
    
    public abstract java.lang.Integer getCustomerNum();
    public abstract void setCustomerNum(java.lang.Integer customerNum);
    
    public abstract java.lang.String getName();
    public abstract void setName(java.lang.String name);
    
    public abstract java.lang.String getAddrLn1();
    public abstract void setAddrLn1(java.lang.String addrLn1);
    
    public abstract java.lang.String getAddrLn2();
    public abstract void setAddrLn2(java.lang.String addrLn2);
    
    public abstract java.lang.String getCity();
    public abstract void setCity(java.lang.String city);
    
    public abstract java.lang.String getState();
    public abstract void setState(java.lang.String state);
    
    public abstract java.lang.String getPhone();
    public abstract void setPhone(java.lang.String phone);
    
    public abstract java.lang.String getFax();
    public abstract void setFax(java.lang.String fax);
    
    public abstract java.lang.String getEmail();
    public abstract void setEmail(java.lang.String email);
    
    public abstract java.lang.Integer getCreditLimit();
    public abstract void setCreditLimit(java.lang.Integer creditLimit);
    
    public abstract java.sql.Date getLastSaleDate();
    public abstract void setLastSaleDate(java.sql.Date lastSaleDate);
    
    public abstract java.sql.Time getLastSaleTime();
    public abstract void setLastSaleTime(java.sql.Time lastSaleTime);
    
    public abstract testPackage.DiscountCodeTblLocal getDiscountCode();
    public abstract void setDiscountCode(testPackage.DiscountCodeTblLocal discountCode);
    
    public abstract testPackage.MicroMarketsTblLocal getZip();
    public abstract void setZip(testPackage.MicroMarketsTblLocal zip);
    
    public abstract java.util.Collection getOrderTblBean();
    public abstract void setOrderTblBean(java.util.Collection orderTblBean);
    
    
    public java.lang.Integer ejbCreate(java.lang.Integer customerNum, java.lang.String name, java.lang.String addrLn1, java.lang.String addrLn2, java.lang.String city, java.lang.String state, java.lang.String phone, java.lang.String fax, java.lang.String email, java.lang.Integer creditLimit, java.sql.Date lastSaleDate, java.sql.Time lastSaleTime, testPackage.DiscountCodeTblLocal discountCode, testPackage.MicroMarketsTblLocal zip)  throws javax.ejb.CreateException {
        if (customerNum == null) {
            throw new javax.ejb.CreateException("The field \"customerNum\" must not be null");
        }
        if (discountCode == null) {
            throw new javax.ejb.CreateException("The field \"discountCode\" must not be null");
        }
        if (zip == null) {
            throw new javax.ejb.CreateException("The field \"zip\" must not be null");
        }
        
        // TODO add additional validation code, throw CreateException if data is not valid
        setCustomerNum(customerNum);
        setName(name);
        setAddrLn1(addrLn1);
        setAddrLn2(addrLn2);
        setCity(city);
        setState(state);
        setPhone(phone);
        setFax(fax);
        setEmail(email);
        setCreditLimit(creditLimit);
        setLastSaleDate(lastSaleDate);
        setLastSaleTime(lastSaleTime);
        
        return null;
    }
    
    public void ejbPostCreate(java.lang.Integer customerNum, java.lang.String name, java.lang.String addrLn1, java.lang.String addrLn2, java.lang.String city, java.lang.String state, java.lang.String phone, java.lang.String fax, java.lang.String email, java.lang.Integer creditLimit, java.sql.Date lastSaleDate, java.sql.Time lastSaleTime, testPackage.DiscountCodeTblLocal discountCode, testPackage.MicroMarketsTblLocal zip) {
        // TODO populate relationships here if appropriate
        setDiscountCode(discountCode);
        setZip(zip);
        
    }
}
