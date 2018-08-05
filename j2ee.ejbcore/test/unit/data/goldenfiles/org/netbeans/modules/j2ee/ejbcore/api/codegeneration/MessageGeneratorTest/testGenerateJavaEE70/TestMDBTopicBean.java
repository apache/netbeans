/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "TestMessageDestination"),
        @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Dups-ok-acknowledge"),
        @ActivationConfigProperty(propertyName = "connectionFactoryLookup", propertyValue = "factoryLookup"),
        @ActivationConfigProperty(propertyName = "clientId", propertyValue = "TestMessageDestination"),
        @ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = "TestMessageDestination"),
        @ActivationConfigProperty(propertyName = "messageSelector", propertyValue = "selector")
    })
public class TestMDBTopicBean implements MessageListener {
    
    public TestMDBTopicBean() {
    }

    @Override
    public void onMessage(Message message) {
    }
    
}
