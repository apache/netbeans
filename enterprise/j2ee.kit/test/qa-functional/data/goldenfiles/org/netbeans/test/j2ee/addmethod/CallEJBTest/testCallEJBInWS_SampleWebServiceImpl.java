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
package sample;

import java.rmi.RemoteException;


/**
 * This is the implementation bean class for the SampleWebService web service.
 * Created 2.6.2005 15:26:52
 * @author lm97939
 */
public class SampleWebServiceImpl implements SampleWebServiceSEI {


    // Enter web service operations here. (Popup menu: Web Service->Add Operation)
    /**
     * Web service operation
     */
    public String operation1() throws RemoteException {
        // TODO implement operation
return lookupSampleSessionBean().sampleBusinessMethod();
    }

    /**
     * Web service operation
     */
    public String operation2(String a, int b) throws java.lang.Exception, java.rmi.RemoteException {
        // TODO implement operation
        return null;
    }

    private sample.SampleSessionRemote lookupSampleSessionBean() {
        try {
            javax.naming.Context c = new javax.naming.InitialContext();
            Object remote = c.lookup("java:comp/env/ejb/SampleSessionBean");
            sample.SampleSessionRemoteHome rv = (sample.SampleSessionRemoteHome) javax.rmi.PortableRemoteObject.narrow(remote, sample.SampleSessionRemoteHome.class);
            return rv.create();
        }
        catch(javax.naming.NamingException ne) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE,"exception caught" ,ne);
            throw new RuntimeException(ne);
        }
        catch(javax.ejb.CreateException ce) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE,"exception caught" ,ce);
            throw new RuntimeException(ce);
        }
        catch(java.rmi.RemoteException re) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE,"exception caught" ,re);
            throw new RuntimeException(re);
        }
    }
}
