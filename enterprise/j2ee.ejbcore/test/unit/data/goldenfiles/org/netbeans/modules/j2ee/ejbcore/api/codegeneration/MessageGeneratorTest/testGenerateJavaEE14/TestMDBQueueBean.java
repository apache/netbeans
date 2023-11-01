/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB21/MessageDrivenEjbClass.java to edit this template
 */

package testGenerateJavaEE14;

import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 *
 * @author {user}
 */
public class TestMDBQueueBean implements MessageDrivenBean, MessageListener {
    
    private MessageDrivenContext context;
    
    // <editor-fold defaultstate="collapsed" desc="EJB infrastructure methods. Click on the + sign on the left to edit the code.">
    
    /**
     * @see javax.ejb.MessageDrivenBean#setMessageDrivenContext(javax.ejb.MessageDrivenContext)
     */
    public void setMessageDrivenContext(MessageDrivenContext aContext) {
        context = aContext;
    }
    
    /**
     * See section 15.4.4 of the EJB 2.0 specification
     * See section 15.7.3 of the EJB 2.1 specification
     */
    public void ejbCreate() {
        // TODO Add code to acquire and use other enterprise resources (DataSource, JMS, enterprise bean, Web services)
    }
    
    /**
     * @see javax.ejb.MessageDrivenBean#ejbRemove()
     */
    public void ejbRemove() {
        // TODO release any resource acquired in ejbCreate.
        // The code here should handle the possibility of not getting invoked
        // See section 15.7.3 of the EJB 2.1 specification
    }
    
    // </editor-fold>
    
    public void onMessage(Message aMessage) {
        // TODO handle incoming message
        // typical implementation will delegate to session bean or application service
    }
    
}
