/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB30/MessageDrivenBean.java to edit this template
 */

package testGenerateJavaEE70;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 *
 * @author {user}
 */
@MessageDriven(activationConfig =  {
        @ActivationConfigProperty(propertyName = "clientId", propertyValue = "TestMessageDestination"),
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "TestMessageDestination"),
        @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Dups-ok-acknowledge"),
        @ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = "TestMessageDestination"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
        @ActivationConfigProperty(propertyName = "connectionFactoryLookup", propertyValue = "factoryLookup"),
        @ActivationConfigProperty(propertyName = "messageSelector", propertyValue = "selector")
    })
public class TestMDBTopicBean implements MessageListener {
    
    public TestMDBTopicBean() {
    }

    @Override
    public void onMessage(Message message) {
    }
    
}
