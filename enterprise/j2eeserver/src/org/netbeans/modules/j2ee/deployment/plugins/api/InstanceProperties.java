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
 * InstanceProperties.java
 *
 */

package org.netbeans.modules.j2ee.deployment.plugins.api;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.util.Map;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;


/**
 *  A way to ask the IDE to store customized information about a server instance
 *  and make it available to a plugin.
 *
 *  Typical usage for create new instance would be like this:
 *      InstanceProperties props = InstanceProperties.getInstanceProperties(url);
 *      if (props == null)
 *          props = InstanceProperties.createInstanceProperties(url, user, password, 
 *                          displayName);
 *      props.setProperty(prop1, value1);
 *      . . .
 *
 * @author George FinKlang
 * @author nn136682
 * @version 0.1
 */
public abstract class InstanceProperties {

    /**
     * URL property, its value is used as a connection string to get the deployment 
     * manager (e.g. "tomcat:home=jakarta-tomcat-5.0.27:base=jakarta-tomcat-5.0.27_base"
     * for Tomcat).
     */
    public static final String URL_ATTR = "url"; //NOI18N

    /**
     * Username property, its value is used by the deployment manager.
     */    
    public static final String USERNAME_ATTR = "username"; //NOI18N
    
    /**
     * Password property, its value is used by the deployment manager.
     */
    public static final String PASSWORD_ATTR = "password"; //NOI18N
    
    /**
     * Display name property, its value is used by IDE to represent server instance.
     */
    public static final String DISPLAY_NAME_ATTR = "displayName"; //NOI18N
    
    /**
     * Remove forbidden property, if its value is set to <code>true</code>, it 
     * won't be allowed to remove the server instance from the server registry.
     */
    public static final String REMOVE_FORBIDDEN = "removeForbidden"; //NOI18N
    
    /**
     * HTTP port property, The port where the instance runs
     */
    public static final String HTTP_PORT_NUMBER = "httpportnumber";
    
      /**
     * Startup timeout property, The number of seconds to allow before assuming 
     *   that a request to start an instance has failed
     * 
     *  @since 1.22
     */
    public static final String STARTUP_TIMEOUT = "startupTimeout";

    /**
     * Shutdown timeout property, The number of seconds to allow before assuming 
     *   that a request to stop an instance has failed
     * 
     *  @since 1.22
     */
    public static final String SHUTDOWN_TIMEOUT = "shutdownTimeout";

    /**
     * Name of the property indicating whether the UI should be
     * handled by j2eeserver api.
     *
     *  @since 1.37
     */
    public static final String REGISTERED_WITHOUT_UI = "registeredWithoutUI";
    
    /**
     * Deployment timeout property, The number of seconds to allow before 
     *   assuming that a request to deploy a project to an instance has failed
     * 
     *  @since 1.22
     */
    public static final String DEPLOYMENT_TIMEOUT = "deploymentTimeout";

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * Returns instance properties for the server instance.
     *
     * @param url the url connection string to get the instance deployment manager.
     * @return the InstanceProperties object, null if instance does not exists.
     */
    public static InstanceProperties getInstanceProperties(String url) {
        ServerInstance inst = ServerRegistry.getInstance().getServerInstance(url);
        if (inst == null)
            return null;
        return inst.getInstanceProperties();
    }
    
    /**
     * Create new instance and returns instance properties for the server instance.
     * 
     * @param url the url connection string to get the instance deployment manager
     * @param username username which is used by the deployment manager.
     * @param password password which is used by the deployment manager.
     * @return the <code>InstanceProperties</code> object, <code>null</code> if 
     *         instance does not exists
     * @exception InstanceCreationException when instance with same url already 
     *            registered.
     *
     * @deprecated use the factory method with displayName parameter.
     */
    @Deprecated
    public static InstanceProperties createInstanceProperties(
            String url, String username, String password) throws InstanceCreationException {
        return createInstanceProperties(url, username, password, null);
    }

    /**
     * Create new instance and returns instance properties for the server instance.
     * This method also register the instance for ui server components such as
     * server node, add wizard dialog and similar. <i>This UI registartion should
     * be avoided and server API/SPI should be used for that directly. This method
     * remains here just for compatibility reasons.</i>
     * 
     * @param url the url connection string to get the instance deployment manager.
     * @param username username which is used by the deployment manager.
     * @param password password which is used by the deployment manager.
     * @param displayName display name which is used by IDE to represent this 
     *        server instance.
     * @return the <code>InstanceProperties</code> object, <code>null</code> if 
     *         instance does not exists.
     * @exception InstanceCreationException when instance with same url already 
     *            registered.
     */
    public static InstanceProperties createInstanceProperties(String url, String username, 
            String password, String displayName) throws InstanceCreationException {

        return createInstanceProperties(url, username, password, displayName, null);
    }
    
    /**
     * Create new instance and returns instance properties for the server instance.
     * This method also register the instance for ui server components such as
     * server node, add wizard dialog and similar. <i>This UI registartion should
     * be avoided and server API/SPI should be used for that directly. This method
     * remains here just for compatibility reasons.</i>
     *
     * @param url the url connection string to get the instance deployment manager.
     * @param username username which is used by the deployment manager.
     * @param password password which is used by the deployment manager.
     * @param displayName display name which is used by IDE to represent this
     *             server instance.
     * @param initialProperties any other properties to set during the instance creation.
     *             If the map contains any of InstanceProperties.URL_ATTR,
     *             InstanceProperties.USERNAME_ATTR, InstanceProperties.PASSWORD_ATTR
     *             or InstanceProperties.DISPLAY_NAME_ATTR they will be ignored
     *             - the explicit parameter values are always used.
     *             <code>null</code> is accepted.
     *
     * @return the <code>InstanceProperties</code> object, <code>null</code> if
     *             instance does not exists.
     * @exception InstanceCreationException when instance with same url already
     *             registered.
     * @since 1.35.0
     */
    public static InstanceProperties createInstanceProperties(String url, String username,
            String password, String displayName, Map<String, String> initialProperties) throws InstanceCreationException {
        ServerRegistry registry = ServerRegistry.getInstance();
        registry.addInstance(url, username, password, displayName, false, false, initialProperties);
        ServerInstance inst = registry.getServerInstance(url);
        InstanceProperties ip = inst.getInstanceProperties();
        return ip;
    }

    /**
     * Create new instance and returns instance properties for the server instance.
     * This method also register the instance for ui server components such as
     * server node, add wizard dialog and similar. When this method is used
     * j2eeserver module will not handle UI for the server.
     *
     * @param url the url connection string to get the instance deployment manager
     * @param username username which is used by the deployment manager
     * @param password password which is used by the deployment manager
     * @param displayName display name which is used by IDE to represent this
     *             server instance
     * @param initialProperties any other properties to set during the instance creation.
     *             If the map contains any of InstanceProperties.URL_ATTR,
     *             InstanceProperties.USERNAME_ATTR, InstanceProperties.PASSWORD_ATTR
     *             or InstanceProperties.DISPLAY_NAME_ATTR they will be ignored
     *             - the explicit parameter values are always used.
     *             <code>null</code> is accepted.
     * @return the <code>InstanceProperties</code> object, <code>null</code> if
     *             instance does not exists
     * @throws InstanceCreationException when instance with same url already
     *             registered
     * @see #removeInstance(String) 
     * @since 1.37.0
     */
    public static InstanceProperties createInstancePropertiesWithoutUI(String url, String username, 
            String password, String displayName, Map<String, String> initialProperties) throws InstanceCreationException {
        ServerRegistry registry = ServerRegistry.getInstance();
        registry.addInstance(url, username, password, displayName, true, false, initialProperties);
        ServerInstance inst = registry.getServerInstance(url);
        InstanceProperties ip = inst.getInstanceProperties();
        return ip;
    }

    /**
     * Create new instance without persisting it and returns instance properties for the server instance.
     * This method also register the instance for ui server components such as
     * server node, add wizard dialog and similar. When this method is used
     * j2eeserver module will not handle UI for the server.
     *
     * @param url the url connection string to get the instance deployment manager
     * @param username username which is used by the deployment manager
     * @param password password which is used by the deployment manager
     * @param displayName display name which is used by IDE to represent this
     *             server instance
     * @param initialProperties any other properties to set during the instance creation.
     *             If the map contains any of InstanceProperties.URL_ATTR,
     *             InstanceProperties.USERNAME_ATTR, InstanceProperties.PASSWORD_ATTR
     *             or InstanceProperties.DISPLAY_NAME_ATTR they will be ignored
     *             - the explicit parameter values are always used.
     *             <code>null</code> is accepted.
     * @return the <code>InstanceProperties</code> object, <code>null</code> if
     *             instance does not exists
     * @throws InstanceCreationException when instance with same url already
     *             registered
     * @see #removeInstance(String) 
     * @since 1.83.0
     */
    public static InstanceProperties createInstancePropertiesNonPersistent(String url, String username, 
            String password, String displayName, Map<String, String> initialProperties) throws InstanceCreationException {
        ServerRegistry registry = ServerRegistry.getInstance();
        registry.addInstance(url, username, password, displayName, true, true, initialProperties);
        ServerInstance inst = registry.getServerInstance(url);
        InstanceProperties ip = inst.getInstanceProperties();
        return ip;
    }

    /**
     * Removes the given server instance from the JavaEE server registry,
     * making it unavailable to JavaEE projects.
     *
     * It the responsibility of the caller to make any changes in server state
     * (e.g. stopping the server) that might be desired or required before
     * calling this method.
     *
     * This method is intended to allow server plugins that registered a JavaEE
     * server instance via {@link #createInstancePropertiesWithoutUI(String, String, String, String, Map) createInstancePropertiesWithoutUI}
     * to remove those instances later.
     *
     * @param url the url connection string to get the instance deployment manager
     * @since 1.41.0
     */
    public static void removeInstance(String url) {
        ServerRegistry.getInstance().removeServerInstance(url);
    }

    /**
     * Returns list of URL strings of all registered instances
     * @return array of URL strings
     */
    public static String[] getInstanceList() {
        return ServerRegistry.getInstance().getInstanceURLs();
    }

    /**
     * Set instance properties.
     * @param props properties to set for this server instance.
     * @exception IllegalStateException when instance already removed or not created yet
     */
    public abstract void setProperties(java.util.Properties props) throws IllegalStateException;

    /**
     * Set instance property
     * @param propname name of property
     * @param value property string value
     * @exception IllegalStateException when instance already removed or not created yet
     */
    public abstract void setProperty(String propname, String value) throws IllegalStateException;
    
    /**
     * Get instance property
     * @param propname name of property
     * @return property string value
     * @exception IllegalStateException when instance already removed or not created yet
     */
    public abstract String getProperty(String propname) throws IllegalStateException;
    
    /**
     * Get instance property keys
     * @return property key enunmeration
     * @exception IllegalStateException when instance already removed or not created yet
     */
    public abstract java.util.Enumeration propertyNames() throws IllegalStateException;
    
    /**
     * Return DeploymentManager associated with this instance.
     *
     * @deprecated without replacement - this method should not be used as there
     * is no api use case for it
     */
    @Deprecated
    public abstract DeploymentManager getDeploymentManager();
    
    /**
     * Ask the server instance to reset cached deployment manager, J2EE
     * management objects and refresh it UI elements.
     */
    public abstract void refreshServerInstance();
    
    /**
     * Add <code>PropertyChangeListener</code> which will be notified of 
     * <code>InstanceProperties</code> changes.
     * 
     * @param listener <code>PropertyChangeListener</code> which will be notified of 
     *        <code>InstanceProperties</code> changes.
     *
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }
    
    /**
     * This method should be called to notify interested listeners when 
     * InstanceProperties change.
     *
     * @param evt A PropertyChangeEvent object describing the event source 
     *   	and the property that has changed.
     */
    protected void firePropertyChange(PropertyChangeEvent evt) {
        propertyChangeSupport.firePropertyChange(evt);
    }
}
