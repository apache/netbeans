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
/*
 * ServerInterface.java
 *
 * Created on October 26, 2004, 11:50 AM
 */

package org.netbeans.modules.j2ee.sun.api;

import java.io.IOException;
import javax.management.MBeanInfo;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.IntrospectionException;
import javax.management.InstanceNotFoundException;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.AttributeList;

import java.rmi.RemoteException;
import java.rmi.ServerException;

import javax.enterprise.deploy.spi.DeploymentManager;
//import org.netbeans.modules.j2ee.sun.share.management.ServerMEJB;
/**
 *
 * @author  Nitya Doraisamy
 */
public interface ServerInterface {
    
    Object getAttribute(ObjectName name, String attribute) throws MBeanException,
        AttributeNotFoundException, InstanceNotFoundException, ReflectionException, RemoteException;
    
    
    AttributeList getAttributes(ObjectName name, String[] attributes) throws
        ReflectionException, InstanceNotFoundException, RemoteException;
    
    MBeanInfo getMBeanInfo(ObjectName name) throws IntrospectionException, InstanceNotFoundException,
        ReflectionException, RemoteException;
    
    Object invoke(ObjectName name, String operationName, Object[] params, String[] signature) throws InstanceNotFoundException,
        MBeanException, ReflectionException, RemoteException;
    
    void setAttribute(ObjectName name, javax.management.Attribute attribute) throws InstanceNotFoundException,
        AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException, RemoteException;
    
    DeploymentManager getDeploymentManager();

    void  setDeploymentManager(DeploymentManager dm);
    
    MBeanServerConnection getMBeanServerConnection() throws RemoteException, ServerException;
     
    /*ServerMEJB*/Object getManagement();
    
    /* check if the dm is ok in term of user name and password,
     * throws an IOexception if this is incorrect
     * oterwise, returns normally
     **/
    void checkCredentials() throws  IOException;
    
    String getWebModuleName(String contextRoot);
}
