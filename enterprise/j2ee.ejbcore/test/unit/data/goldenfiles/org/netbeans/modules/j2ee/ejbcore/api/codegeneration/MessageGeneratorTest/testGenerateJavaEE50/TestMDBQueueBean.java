/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
@MessageDriven(mappedName = "TestMessageDestination", activationConfig =  {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
    })
public class TestMDBQueueBean implements MessageListener {
    
    public TestMDBQueueBean() {
    }

    @Override
    public void onMessage(Message message) {
    }
    
}
