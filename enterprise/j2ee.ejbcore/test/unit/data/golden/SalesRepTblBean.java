package testPackage;

import javax.ejb.*;

/**
 * This is the bean class for the SalesRepTblBean enterprise bean.
 * Created Kdysi
 * @author Kdosi
 */
public abstract class SalesRepTblBean implements javax.ejb.EntityBean, testPackage.SalesRepTblLocalBusiness {
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
    
    
    public abstract java.lang.Integer getSalesRepNum();
    public abstract void setSalesRepNum(java.lang.Integer salesRepNum);
    
    public abstract java.lang.String getFirstName();
    public abstract void setFirstName(java.lang.String firstName);
    
    public abstract java.lang.String getLastName();
    public abstract void setLastName(java.lang.String lastName);
    
    public abstract java.lang.Integer getQuota();
    public abstract void setQuota(java.lang.Integer quota);
    
    public abstract java.math.BigDecimal getYtdSales();
    public abstract void setYtdSales(java.math.BigDecimal ytdSales);
    
    public abstract java.math.BigDecimal getLastYrSales();
    public abstract void setLastYrSales(java.math.BigDecimal lastYrSales);
    
    public abstract java.math.BigDecimal getCommissionRate();
    public abstract void setCommissionRate(java.math.BigDecimal commissionRate);
    
    public abstract java.lang.Integer getTravelAllow();
    public abstract void setTravelAllow(java.lang.Integer travelAllow);
    
    public abstract java.sql.Date getHireDate();
    public abstract void setHireDate(java.sql.Date hireDate);
    
    public abstract java.lang.Float getTwoYrSalesGoal();
    public abstract void setTwoYrSalesGoal(java.lang.Float twoYrSalesGoal);
    
    public abstract java.util.Collection getSalesRepDataTblBean();
    public abstract void setSalesRepDataTblBean(java.util.Collection salesRepDataTblBean);
    
    public abstract java.util.Collection getOrderTblBean();
    public abstract void setOrderTblBean(java.util.Collection orderTblBean);
    
    public abstract testPackage.OfficeTblLocal getOfficeNum();
    public abstract void setOfficeNum(testPackage.OfficeTblLocal officeNum);
    
    
    public java.lang.Integer ejbCreate(java.lang.Integer salesRepNum, java.lang.String firstName, java.lang.String lastName, java.lang.Integer quota, java.math.BigDecimal ytdSales, java.math.BigDecimal lastYrSales, java.math.BigDecimal commissionRate, java.lang.Integer travelAllow, java.sql.Date hireDate, java.lang.Float twoYrSalesGoal, testPackage.OfficeTblLocal officeNum)  throws javax.ejb.CreateException {
        if (salesRepNum == null) {
            throw new javax.ejb.CreateException("The field \"salesRepNum\" must not be null");
        }
        if (officeNum == null) {
            throw new javax.ejb.CreateException("The field \"officeNum\" must not be null");
        }
        
        // TODO add additional validation code, throw CreateException if data is not valid
        setSalesRepNum(salesRepNum);
        setFirstName(firstName);
        setLastName(lastName);
        setQuota(quota);
        setYtdSales(ytdSales);
        setLastYrSales(lastYrSales);
        setCommissionRate(commissionRate);
        setTravelAllow(travelAllow);
        setHireDate(hireDate);
        setTwoYrSalesGoal(twoYrSalesGoal);
        
        return null;
    }
    
    public void ejbPostCreate(java.lang.Integer salesRepNum, java.lang.String firstName, java.lang.String lastName, java.lang.Integer quota, java.math.BigDecimal ytdSales, java.math.BigDecimal lastYrSales, java.math.BigDecimal commissionRate, java.lang.Integer travelAllow, java.sql.Date hireDate, java.lang.Float twoYrSalesGoal, testPackage.OfficeTblLocal officeNum) {
        // TODO populate relationships here if appropriate
        setOfficeNum(officeNum);
        
    }
}
