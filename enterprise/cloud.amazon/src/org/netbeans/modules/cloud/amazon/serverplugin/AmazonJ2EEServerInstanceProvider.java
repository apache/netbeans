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
package org.netbeans.modules.cloud.amazon.serverplugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.cloud.amazon.AmazonInstance;
import org.netbeans.modules.cloud.amazon.AmazonInstanceManager;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.spi.server.ServerInstanceFactory;
import org.netbeans.spi.server.ServerInstanceProvider;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;

/**
 *
 */
public final class AmazonJ2EEServerInstanceProvider implements ServerInstanceProvider, ChangeListener {

    private ChangeSupport listeners;
    private List<ServerInstance> instances;
    private static AmazonJ2EEServerInstanceProvider instance;
    
    private static Logger LOG = Logger.getLogger(AmazonJ2EEServerInstanceProvider.class.getName());
    
    private AmazonJ2EEServerInstanceProvider() {
        listeners = new ChangeSupport(this);
        instances = Collections.<ServerInstance>emptyList();
        //refreshServers();
    }
    
    public static synchronized AmazonJ2EEServerInstanceProvider getProvider() {
        if (instance == null) {
            instance = new AmazonJ2EEServerInstanceProvider();
            AmazonInstanceManager.getDefault().addChangeListener(instance);
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

    private void refreshServersSynchronously() {
        List<ServerInstance> servers = new ArrayList<ServerInstance>();
        for (AmazonInstance ai : AmazonInstanceManager.getDefault().getInstances()) {
            for (AmazonJ2EEInstance inst : ai.readJ2EEServerInstances()) {
                ServerInstance si = ServerInstanceFactory.createServerInstance(new AmazonJ2EEServerInstanceImplementation(inst));
                InstanceProperties ip = InstanceProperties.getInstanceProperties(inst.getId());
                if (ip == null) {
                    Map<String, String> props = new HashMap<String, String>();
                    props.put(AmazonDeploymentFactory.IP_ENVIRONMENT_ID, inst.getEnvironmentId());
                    props.put(AmazonDeploymentFactory.IP_APPLICATION_NAME, inst.getApplicationName());
                    props.put(AmazonDeploymentFactory.IP_KEY_ID, ai.getKeyId());
                    props.put(AmazonDeploymentFactory.IP_KEY, ai.getKey());
                    props.put(AmazonDeploymentFactory.IP_CONTAINER_TYPE, inst.getContainerType());
                    props.put(InstanceProperties.URL_ATTR, inst.getId());
                    props.put(AmazonDeploymentFactory.IP_REGION_URL, ai.getRegionURL());
                    props.put(AmazonDeploymentFactory.IP_REGION_CODE, ai.getRegionCode());
                    try {
                        ip = InstanceProperties.createInstancePropertiesNonPersistent(inst.getId(), 
                                ai.getKeyId(), ai.getKey(), inst.getDisplayName(), props);
                    } catch (InstanceCreationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                inst.setInstance(si);
                servers.add(si);
            }
        }
        instances = servers;
        listeners.fireChange();
    }
    
    public final Future<Void> refreshServers() {
        return AmazonInstance.runAsynchronously(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                try {
                    refreshServersSynchronously();
                    
                    // TODO: set state of all amazon cloud instances to be in OK mode
                    
                } catch (RuntimeException e) {
                    LOG.log(Level.INFO, "refreshServers failed. perhaps AWS is not accessible?", e);
                    
                    // TODO: set state of all amazon cloud instances to be in ERROR mode
                    
                }
                return null;
            }
        });
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        refreshServers();
    }
}
