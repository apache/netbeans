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
package org.netbeans.modules.cloud.amazon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.spi.server.ServerInstanceFactory;
import org.netbeans.spi.server.ServerInstanceProvider;
import org.openide.util.ChangeSupport;

/**
 * Provider of registered Amazon accounts in the IDE.
 */
public class AmazonServerInstanceProvider implements ServerInstanceProvider, ChangeListener {

    private ChangeSupport listeners;
    private List<ServerInstance> instances;
    private static AmazonServerInstanceProvider instance;
    
    private AmazonServerInstanceProvider() {
        listeners = new ChangeSupport(this);
        instances = Collections.<ServerInstance>emptyList();
        AmazonInstanceManager.getDefault().addChangeListener(this);
        refreshServers();
    }
    
    public static synchronized AmazonServerInstanceProvider getProvider() {
        if (instance == null) {
            instance = new AmazonServerInstanceProvider();
        }
        return instance;
    }

    @Override
    public List<ServerInstance> getInstances() {
        return instances;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        listeners.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        listeners.removeChangeListener(listener);
    }
    
    private void refreshServers() {
        List<ServerInstance> servers = new ArrayList<ServerInstance>();
        for (AmazonInstance ai : AmazonInstanceManager.getDefault().getInstances()) {
            ServerInstance si = ServerInstanceFactory.createServerInstance(new AmazonServerInstanceImplementation(ai));
            ai.setServerInstance(si);
            servers.add(si);
        }
        this.instances = servers;
        listeners.fireChange();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        refreshServers();
    }
}
