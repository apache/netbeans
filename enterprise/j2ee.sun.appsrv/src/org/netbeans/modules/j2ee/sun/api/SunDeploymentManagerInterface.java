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

package org.netbeans.modules.j2ee.sun.api;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashMap;
import org.openide.nodes.Node;

/**
 * Extensions specific to our sun deployment manager
 * @author  vkraemer
 */
public interface SunDeploymentManagerInterface extends Node.Cookie{

    /* return the user name used for this deploymment manager*/
    String getUserName();

    /* return the user password used for this deploymment manager*/
    String getPassword();
    
    /* set the user name used for this deploymment manager*/
    void setUserName(String name);
    
    /* set the user password used for this deploymment manager*/
    void  setPassword(String pw);
    
    /* return the hostname name used for this deploymment manager*/
    String getHost();
    
    /* return the port used for this deploymment manager*/
    int getPort();
    
    /*
     * return the real http port for the server. Usually, it is "8080", or null if the server is not running
     *
     **/
     String getNonAdminPortNumber() ;
     
    /* tells if  deploymment manager is this local machine or not*/
    boolean isLocal();
    
    /* return true is this  deploymment manager is running*/
    boolean isRunning();
    /* return true is this  deploymment manager is running, 
     * if forced is true, no caching of the value is done, so the latest status is available in real time
     */
    boolean isRunning(boolean forced);
    
        /* return true is this  deploymment manager needs a restart, because of changes in admin configuration*/
    public boolean isRestartNeeded();
    
    /* return true is this  deploymment manager is secure, i.e is using https instead of http protocol*/
    boolean isSecure();
    
   ServerInterface/* ServerMEJB*/ getManagement();
    
//    MBeanServerConnection getMBeanServerConnection() throws RemoteException, ServerException;
   /*
    * necessary to fix some jpda bug due to dt_socket in Windows only platform
    */
   void fixJVMDebugOptions() throws java.rmi.RemoteException;
   String getDebugAddressValue() throws java.rmi.RemoteException;
   boolean isDebugSharedMemory() throws java.rmi.RemoteException;
   
   ResourceConfiguratorInterface getResourceConfigurator();
   CmpMappingProvider getSunCmpMapper();
   
   boolean isSuspended();
   /*
    * force a refresh of the internal Deployment manager. 
    * Sometimes useful to reset a few calculated values.
    *
    **/
   void refreshDeploymentManager();
   
   /*
    * return the App Server installation root used for getting the extra jar for this Deployment
    * manager
    * might return null if not a valid directory
    * usually, this is not stored within the DM URI and correctly calculated when ytou create the DM.
    
    */
   File  getPlatformRoot();
   
   HashMap getSunDatasourcesFromXml();
   
   HashMap getConnPoolsFromXml();

   HashMap getAdminObjectResourcesFromXml();
   
   void createSampleDataSourceinDomain();
    
       /** Registers new listener. */
    void addPropertyChangeListener(PropertyChangeListener l);
    
    /** Unregister the listener. */
    void removePropertyChangeListener(PropertyChangeListener l);
    
    boolean grabInnerDM(Thread h, boolean returnInsteadOfWaiting);
    
    void releaseInnerDM(Thread h);
    
    int getAppserverVersion(); 
}
