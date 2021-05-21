
package cmp;


/**
 * This is the business interface for Customer enterprise bean.
 */
public interface CustomerLocalBusiness {
    public abstract java.lang.Long getId();

    public abstract java.lang.String getLastName();

    public abstract void setLastName(java.lang.String lastName);

    public abstract java.lang.String getFirstName();

    public abstract void setFirstName(java.lang.String firstName);

    void setTestChangeCMPFieldName(boolean testCMPField);

    
}
