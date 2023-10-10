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
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "TestMessageDestination2"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
    })
public class TestMDBQueueBean2 implements MessageListener {
    
    public TestMDBQueueBean2() {
    }

    @Override
    public void onMessage(Message message) {
    }
    
}
