/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB30/MessageDrivenBean.java to edit this template
 */

package testGenerateJavaEE50;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 *
 * @author {user}
 */
@MessageDriven(mappedName = "TestMDBTopicBean", activationConfig =  {
        @ActivationConfigProperty(propertyName = "clientId", propertyValue = "TestMDBTopicBean"),
        @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable"),
        @ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = "TestMDBTopicBean"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic")
    })
public class TestMDBTopicBean implements MessageListener {
    
    public TestMDBTopicBean() {
    }

    @Override
    public void onMessage(Message message) {
    }
    
}
