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
package test.expand;
    
import javax.ejb.*;
  
/**
 * This is the bean class for the ExpandTest015Bean enterprise bean.
 * Created 4.3.2005 11:03:09
 * @author lm97939
 */
public class ExpandTest015Bean implements javax.ejb.SessionBean, test.expand.ExpandTest015RemoteBusiness {
    private javax.ejb.SessionContext context;

    // <editor-fold defaultstate="collapsed" desc="EJB infrastructure methods. Click the + sign on the left to edit the code.">
    // TODO Add code to acquire and use other enterprise resources (DataSource, JMS, enterprise bean, Web services)
    // TODO Add business methods
    /**
    * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
    */
    public void setSessionContext (javax.ejb.SessionContext aContext) {
    context = aContext;
    }

    /**
    * @see javax.ejb.SessionBean#ejbActivate()
    */
    public void ejbActivate () {

    }

    /**
    * @see javax.ejb.SessionBean#ejbPassivate()
    */
    public void ejbPassivate () {

    }

    /**
    * @see javax.ejb.SessionBean#ejbRemove()
    */
    public void ejbRemove () {

    }
    // </editor-fold>
    
    /**
    * See section 7.10.3 of the EJB 2.0 specification
    * See section 7.11.3 of the EJB 2.1 specification
    */
    public void ejbCreate () {
        // TODO implement ejbCreate if necessary, acquire resources
        // This method has access to the JNDI context so resource aquisition
        // spanning all methods can be performed here such as home interfaces
        // and data sources. 
    }
 
    

    // Enter business methods below. (Right-click in editor and choose 
    // Enterprise JavaBeans (EJB) > Add Business Method)

    public String testMethod() {
        //TODO implement testMethod
        return null;
    }
    

    
}
  
