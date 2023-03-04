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
 * InstancePropertiesImpl.java
 *
 * Created on December 4, 2003, 6:11 PM
 */

package org.netbeans.modules.j2ee.deployment.impl;

import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener;
import org.openide.util.Exceptions;

/**
 *
 * @author  nn136682
 */
public class DefaultInstancePropertiesImpl extends DeletableInstanceProperties implements InstanceListener {

    private final String url;

    private FileObject fo;

    /** Creates a new instance of InstancePropertiesImpl */
    public DefaultInstancePropertiesImpl(String url) {
        this.url = url;
    }

    @Override
    public void instanceRemoved(String instance) {
        if (instance != null && url.equals(instance)) {
            fo = null;
        }
    }

    @Override
    public void instanceAdded(String instance) {
        // noop
    }

    public void changeDefaultInstance(String oldInstance, String newInstance){
        // noop
    }

    @Override
    public String getProperty(String propname) throws IllegalStateException {
        Object propValue = getFileObject().getAttribute(propname);
        String propString = propValue == null ? null : propValue.toString();
        if (InstanceProperties.PASSWORD_ATTR.equals(propname) && propValue == null) {
            propString = ServerRegistry.readPassword(url);
        }
        return propString;
    }

    @Override
    public java.util.Enumeration propertyNames() throws IllegalStateException {
        return getFileObject().getAttributes();
    }

    @Override
    public void setProperty(String propname, String value) throws IllegalStateException {
        try {
            String oldValue = getProperty(propname);
            if (InstanceProperties.PASSWORD_ATTR.equals(propname)) {
                ServerRegistry.savePassword(url, value,
                        NbBundle.getMessage(DefaultInstancePropertiesImpl.class, "MSG_KeyringDefaultDisplayName"));
                getFileObject().setAttribute(propname, null);
            } else {
                getFileObject().setAttribute(propname, value);
            }
            firePropertyChange(new PropertyChangeEvent(this, propname, oldValue, value));
        } catch (IOException ioe) {
            String message = NbBundle.getMessage(DefaultInstancePropertiesImpl.class, "MSG_InstanceNotExists", url);
            throw new IllegalStateException(Exceptions.attachLocalizedMessage(ioe, message));
        }
    }

    @Override
    public void setProperties(java.util.Properties props) throws IllegalStateException {
        getFileObject(); // eager check we can manipulati it

        java.util.Enumeration propNames = props.propertyNames();
        while (propNames.hasMoreElements()) {
            String propName = (String) propNames.nextElement();
            String propValue = props.getProperty(propName);
            setProperty(propName, propValue);
        }
    }

    @Override
    public javax.enterprise.deploy.spi.DeploymentManager getDeploymentManager() {
        boolean assertsEnabled = false;
        assert assertsEnabled = true;
        if (assertsEnabled) {
            return new LazyDeploymentManager(new LazyDeploymentManager.DeploymentManagerProvider() {

                @Override
                public DeploymentManager getDeploymentManager() {
                    return LazyDeploymentManager.getDeploymentManager(url);
                }
            });
        }
        return LazyDeploymentManager.getDeploymentManager(url);
    }

    @Override
    public void refreshServerInstance() {
        ServerRegistry registry = ServerRegistry.getInstance();
        ServerInstance inst = registry.getServerInstance(url);
        if (inst != null) {
            inst.refresh();
        }
    }

    @Override
    boolean isDeleted() {
        if (fo != null) {
            return false;
        }
        if (ServerRegistry.getInstance().getServerInstance(url) == null) {
            return true;
        }
        return ServerRegistry.getInstanceFileObject(url) == null;
    }

    private FileObject getFileObject() {
        if (fo == null) {
            ServerInstance instance = ServerRegistry.getInstance().getServerInstance(url);
            if (instance == null) {
                throw new IllegalStateException(
                    (NbBundle.getMessage(DefaultInstancePropertiesImpl.class, "MSG_InstanceNotExists", url)));
            }
            fo = ServerRegistry.getInstanceFileObject(url);
            if (fo == null) {
                throw new IllegalStateException(
                (NbBundle.getMessage(DefaultInstancePropertiesImpl.class, "MSG_InstanceNotExists", url)));
            }
        }
        return fo;
    }
}
