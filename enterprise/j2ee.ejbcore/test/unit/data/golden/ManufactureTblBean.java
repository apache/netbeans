package testPackage;

import javax.ejb.*;

/**
 * This is the bean class for the ManufactureTblBean enterprise bean.
 * Created Kdysi
 * @author Kdosi
 */
public abstract class ManufactureTblBean implements javax.ejb.EntityBean, testPackage.ManufactureTblLocalBusiness {
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
    
    
    public abstract java.lang.Integer getMfrNum();
    public abstract void setMfrNum(java.lang.Integer mfrNum);
    
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
    
    public abstract java.lang.String getZip();
    public abstract void setZip(java.lang.String zip);
    
    public abstract java.lang.String getPhone();
    public abstract void setPhone(java.lang.String phone);
    
    public abstract java.lang.String getFax();
    public abstract void setFax(java.lang.String fax);
    
    public abstract java.lang.String getEmail();
    public abstract void setEmail(java.lang.String email);
    
    public abstract java.lang.String getRep();
    public abstract void setRep(java.lang.String rep);
    
    public abstract java.lang.Boolean getLocalCompany();
    public abstract void setLocalCompany(java.lang.Boolean localCompany);
    
    public abstract java.util.Collection getProductTblBean();
    public abstract void setProductTblBean(java.util.Collection productTblBean);
    
    
    public java.lang.Integer ejbCreate(java.lang.Integer mfrNum, java.lang.String name, java.lang.String addrLn1, java.lang.String addrLn2, java.lang.String city, java.lang.String state, java.lang.String zip, java.lang.String phone, java.lang.String fax, java.lang.String email, java.lang.String rep, java.lang.Boolean localCompany)  throws javax.ejb.CreateException {
        if (mfrNum == null) {
            throw new javax.ejb.CreateException("The field \"mfrNum\" must not be null");
        }
        
        // TODO add additional validation code, throw CreateException if data is not valid
        setMfrNum(mfrNum);
        setName(name);
        setAddrLn1(addrLn1);
        setAddrLn2(addrLn2);
        setCity(city);
        setState(state);
        setZip(zip);
        setPhone(phone);
        setFax(fax);
        setEmail(email);
        setRep(rep);
        setLocalCompany(localCompany);
        
        return null;
    }
    
    public void ejbPostCreate(java.lang.Integer mfrNum, java.lang.String name, java.lang.String addrLn1, java.lang.String addrLn2, java.lang.String city, java.lang.String state, java.lang.String zip, java.lang.String phone, java.lang.String fax, java.lang.String email, java.lang.String rep, java.lang.Boolean localCompany) {
        // TODO populate relationships here if appropriate
        
    }
}
