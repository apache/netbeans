/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package pack;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.PrePassivate;
import javax.ejb.Remove;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 *
 * @author den
 */
@MessageDriven(mappedName = "a", activationConfig =  {
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
    })
public class NewMessageBean implements MessageListener {
    
    public NewMessageBean() {
    }

    @Remove
    public void onMessage(Message message) {
    }

    public void operation(){
        
    }

    public void ejbActivate(){

    }

    public void ejbPassivate(){

    }

    public void ejbRemove(){

    }

    public void setSessionContext(){

    }

    public void setEntityContext(){

    }

    public void unsetEntityContext(){

    }

    public void setMessageDrivenContext(){

    }

    @PrePassivate
    public void ejbStore(){
        
    }
    
}
