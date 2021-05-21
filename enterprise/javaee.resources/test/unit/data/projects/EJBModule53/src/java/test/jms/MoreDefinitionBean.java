package test.jms;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSDestinationDefinition;
import javax.jms.JMSDestinationDefinitions;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
@JMSDestinationDefinitions({
    @JMSDestinationDefinition(className = "javax.ejb.Queue", name = "queueInMulti1"),
    @JMSDestinationDefinition(className = "javax.ejb.Queue", name = "queueInMulti2"),
    @JMSDestinationDefinition(className = "javax.ejb.Topic", name = "queueInMulti3"),
    @JMSDestinationDefinition(className = "javax.ejb.Topic", name = "queueInMulti4"),
})
@MessageDriven(mappedName = "", activationConfig = {
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class MoreDefinitionBean implements MessageListener {

    public MoreDefinitionBean() {
    }

    @Override
    public void onMessage(Message message) {
    }
}
