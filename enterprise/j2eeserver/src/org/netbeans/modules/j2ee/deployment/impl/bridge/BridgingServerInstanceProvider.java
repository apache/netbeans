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

package org.netbeans.modules.j2ee.deployment.impl.bridge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeListener;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener;
import org.netbeans.modules.j2ee.deployment.impl.Server;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.openide.util.ChangeSupport;

/**
 *
 * @author Petr Hejl
 */
public class BridgingServerInstanceProvider implements org.netbeans.spi.server.ServerInstanceProvider, InstanceListener {

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private final Server server;

    private Map<org.netbeans.modules.j2ee.deployment.impl.ServerInstance, BridgingServerInstance> instances =
            new HashMap<org.netbeans.modules.j2ee.deployment.impl.ServerInstance, BridgingServerInstance>();

    public BridgingServerInstanceProvider(Server server) {
        assert server != null : "Server must not be null"; // NOI18N
        this.server = server;
    }

    public final void addInstanceListener() {
        ServerRegistry.getInstance().addInstanceListener(this);
    }

    public final void removeInstanceListener() {
        ServerRegistry.getInstance().removeInstanceListener(this);
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public void changeDefaultInstance(String oldServerInstanceID, String newServerInstanceID) {

    }

    public void instanceAdded(String serverInstanceID) {
        if (server.handlesUri(serverInstanceID)) {
            changeSupport.fireChange();
        }
    }

    public void instanceRemoved(String serverInstanceID) {
        InstanceProperties props = InstanceProperties.getInstanceProperties(serverInstanceID);
        if (server.handlesUri(serverInstanceID) && (props == null || isRegisteredWithUI(props))) {
            changeSupport.fireChange();
        }
    }

    // TODO we could slightly optimize this by cacheing
    public synchronized List<ServerInstance> getInstances() {
        refreshCache();
        List<ServerInstance> instancesList = new  ArrayList<ServerInstance>(instances.size());
        for (BridgingServerInstance instance : instances.values()) {
            instancesList.add(instance.getCommonInstance());
        }
        return instancesList;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BridgingServerInstanceProvider other = (BridgingServerInstanceProvider) obj;
        if (this.server != other.server && (this.server == null || !this.server.equals(other.server))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.server != null ? this.server.hashCode() : 0);
        return hash;
    }

    public synchronized ServerInstance getBridge(org.netbeans.modules.j2ee.deployment.impl.ServerInstance instance) {
        refreshCache();
        BridgingServerInstance bridgingInstance = instances.get(instance);
        return bridgingInstance == null ? null : bridgingInstance.getCommonInstance();
    }

    private synchronized void refreshCache() {
        List<org.netbeans.modules.j2ee.deployment.impl.ServerInstance> toRemove = new ArrayList<org.netbeans.modules.j2ee.deployment.impl.ServerInstance>(instances.keySet());

        for (org.netbeans.modules.j2ee.deployment.impl.ServerInstance instance : ServerRegistry.getInstance().getServerInstances()) {
            if (instance.getServer().equals(server) && isRegisteredWithUI(instance.getInstanceProperties())) {
                if (!instances.containsKey(instance)) {
                    instances.put(instance, BridgingServerInstance.createInstance(instance));
                } else {
                    toRemove.remove(instance);
                }
            }
        }

        instances.keySet().removeAll(toRemove);
    }

    private boolean isRegisteredWithUI(InstanceProperties props) {
        String withoutUI = props.getProperty(InstanceProperties.REGISTERED_WITHOUT_UI);
        if (withoutUI == null) {
            return true;
        }
        return !Boolean.valueOf(withoutUI);
    }
}
