/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
    
///    MBeanServerConnection getMBeanServerConnection() throws RemoteException, ServerException;
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
