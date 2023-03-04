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
package org.netbeans.modules.j2ee.deployment.impl;

import java.beans.PropertyChangeEvent;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
public class MemoryInstancePropertiesImpl extends DeletableInstanceProperties implements InstanceListener {

    private Map<String, String> properties = new HashMap<String, String>();

    private final String url;

    public MemoryInstancePropertiesImpl(ServerInstance instance) {
        this(instance.getUrl());
    }

    public MemoryInstancePropertiesImpl(String url) {
        this.url = url;
    }

    @Override
    public DeploymentManager getDeploymentManager() {
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
    public String getProperty(String propname) throws IllegalStateException {
        synchronized (this) {
            return getProperties().get(propname);
        }
    }

    @Override
    public Enumeration propertyNames() throws IllegalStateException {
        synchronized (this) {
            return Collections.enumeration(new HashSet<String>(getProperties().keySet()));
        }
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
    public void setProperties(Properties props) throws IllegalStateException {
        getProperties(); // eager check we can manipulati it

        java.util.Enumeration propNames = props.propertyNames();
        while (propNames.hasMoreElements()) {
            String propName = (String) propNames.nextElement();
            String propValue = props.getProperty(propName);
            setProperty(propName, propValue);
        }
    }

    @Override
    public void setProperty(String propname, String value) throws IllegalStateException {
        String oldValue = null;
        synchronized (this) {
            oldValue = getProperties().put(propname, value);
        }
        firePropertyChange(new PropertyChangeEvent(this, propname, oldValue, value));
    }

    @Override
    public void instanceAdded(String serverInstanceID) {
        // noop
    }

    @Override
    public void instanceRemoved(String serverInstanceID) {
        if (serverInstanceID != null && url.equals(serverInstanceID)) {
            // we are just defensive
            synchronized (this) {
                properties = null;
            }
        }
    }

    @Override
    boolean isDeleted() {
        synchronized (this) {
            return properties == null;
        }
    }

    private synchronized Map<String, String> getProperties() {
        if (properties == null) {
            throw new IllegalStateException(
                (NbBundle.getMessage(MemoryInstancePropertiesImpl.class, "MSG_InstanceNotExists", url)));
        }
        return properties;
    }
}
