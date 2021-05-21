/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.jms;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSDestinationDefinition;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
@JMSDestinationDefinition(className = "javax.ejb.Queue", name = "queueInOneBean")
@MessageDriven(mappedName = "queueInOneBean", activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class OneDefinitionBean implements MessageListener {

    public OneDefinitionBean() {
    }

    @Override
    public void onMessage(Message message) {
    }
}
