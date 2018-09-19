/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package test;

import javax.ejb.*;

/**
 * This is the bean class for the TestingMessageBean enterprise bean.
 * Created 19.5.2005 17:02:38
 * @author lm97939
 */
public class TestingMessageBean implements javax.ejb.MessageDrivenBean, javax.jms.MessageListener {
    private javax.ejb.MessageDrivenContext context;

    // <editor-fold defaultstate="collapsed" desc="EJB infrastructure methods. Click on the + sign on the left to edit the code.">

    /**
     * @see javax.ejb.MessageDrivenBean#setMessageDrivenContext(javax.ejb.MessageDrivenContext)
     */
    public void setMessageDrivenContext(javax.ejb.MessageDrivenContext aContext) {
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

    public void onMessage(javax.jms.Message aMessage) {
        // TODO handle incoming message
        // typical implementation will delegate to session bean or application service
    }

}
