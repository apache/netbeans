package testPackage;

import javax.ejb.*;

/**
 * This is the bean class for the OfficeTblBean enterprise bean.
 * Created Kdysi
 * @author Kdosi
 */
public abstract class OfficeTblBean implements javax.ejb.EntityBean, testPackage.OfficeTblLocalBusiness {
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
    
    
    public abstract java.lang.Integer getOfficeNum();
    public abstract void setOfficeNum(java.lang.Integer officeNum);
    
    public abstract java.lang.String getCity();
    public abstract void setCity(java.lang.String city);
    
    public abstract java.lang.String getRegion();
    public abstract void setRegion(java.lang.String region);
    
    public abstract java.lang.Integer getTargetSales();
    public abstract void setTargetSales(java.lang.Integer targetSales);
    
    public abstract java.sql.Date getOpenDate();
    public abstract void setOpenDate(java.sql.Date openDate);
    
    public abstract java.sql.Date getCloseDate();
    public abstract void setCloseDate(java.sql.Date closeDate);
    
    public abstract testPackage.OfficeTypeCodeTblLocal getTypeCode();
    public abstract void setTypeCode(testPackage.OfficeTypeCodeTblLocal typeCode);
    
    public abstract java.util.Collection getSalesRepTblBean();
    public abstract void setSalesRepTblBean(java.util.Collection salesRepTblBean);
    
    
    public java.lang.Integer ejbCreate(java.lang.Integer officeNum, java.lang.String city, java.lang.String region, java.lang.Integer targetSales, java.sql.Date openDate, java.sql.Date closeDate, testPackage.OfficeTypeCodeTblLocal typeCode)  throws javax.ejb.CreateException {
        if (officeNum == null) {
            throw new javax.ejb.CreateException("The field \"officeNum\" must not be null");
        }
        if (typeCode == null) {
            throw new javax.ejb.CreateException("The field \"typeCode\" must not be null");
        }
        
        // TODO add additional validation code, throw CreateException if data is not valid
        setOfficeNum(officeNum);
        setCity(city);
        setRegion(region);
        setTargetSales(targetSales);
        setOpenDate(openDate);
        setCloseDate(closeDate);
        
        return null;
    }
    
    public void ejbPostCreate(java.lang.Integer officeNum, java.lang.String city, java.lang.String region, java.lang.Integer targetSales, java.sql.Date openDate, java.sql.Date closeDate, testPackage.OfficeTypeCodeTblLocal typeCode) {
        // TODO populate relationships here if appropriate
        setTypeCode(typeCode);
        
    }
}
