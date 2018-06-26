/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
