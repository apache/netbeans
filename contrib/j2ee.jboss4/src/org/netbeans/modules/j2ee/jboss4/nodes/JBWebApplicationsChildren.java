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

package org.netbeans.modules.j2ee.jboss4.nodes;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import org.netbeans.modules.j2ee.jboss4.JBDeploymentManager;
import org.netbeans.modules.j2ee.jboss4.JBRemoteAction;
import org.netbeans.modules.j2ee.jboss4.JBoss5ProfileServiceProxy;
import org.netbeans.modules.j2ee.jboss4.nodes.actions.Refreshable;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * It describes children nodes of the Web Applications node. Implements
 * Refreshable interface and due to it can be refreshed via
 * ResreshModulesAction.
 *
 * @author Michal Mocnak
 */
public class JBWebApplicationsChildren extends JBAsyncChildren implements Refreshable {

    private static final Logger LOGGER = Logger.getLogger(JBWebApplicationsChildren.class.getName());

    private static final Set<String> SYSTEM_WEB_APPLICATIONS = new HashSet<String>();
    static {
        Collections.addAll(SYSTEM_WEB_APPLICATIONS,
                "jbossws-context", "jmx-console", "jbossws", "jbossws",
                "web-console", "invoker", "jbossmq-httpil");
    }

    private final JBAbilitiesSupport abilitiesSupport;

    private final Lookup lookup;

    public JBWebApplicationsChildren(Lookup lookup) {
        this.lookup = lookup;
        this.abilitiesSupport = new JBAbilitiesSupport(lookup);
    }

    public void updateKeys() {
        setKeys(new Object[]{Util.WAIT_NODE});
        getExecutorService().submit(abilitiesSupport.isJB7x() ? new JBoss7WebNodeUpdater() : new JBossWebNodeUpdater(), 0);
    }

    class JBossWebNodeUpdater implements Runnable {

        List keys = new ArrayList();

        @Override
        public void run() {
            try {
                final JBDeploymentManager dm = (JBDeploymentManager) lookup.lookup(JBDeploymentManager.class);
                dm.invokeRemoteAction(new JBRemoteAction<Void>() {

                    @Override
                    public Void action(MBeanServerConnection connection, JBoss5ProfileServiceProxy profileService) throws Exception {
                        // Query to the jboss server
                        ObjectName searchPattern;
                        if (abilitiesSupport.isRemoteManagementSupported()
                                && (abilitiesSupport.isJB4x() || abilitiesSupport.isJB6x())) {
                            searchPattern = new ObjectName("jboss.management.local:j2eeType=WebModule,J2EEApplication=null,*"); // NOI18N
                            }
                            else {
                            searchPattern = new ObjectName("jboss.web:j2eeType=WebModule,J2EEApplication=none,*"); // NOI18N
                        }

                        Method method = connection.getClass().getMethod("queryMBeans", new Class[]  {ObjectName.class, QueryExp.class});
                        method = Util.fixJava4071957(method);
                        Set managedObj = (Set) method.invoke(connection, new Object[]  {searchPattern, null});

                        // Query results processing
                        for (Iterator it = managedObj.iterator(); it.hasNext();) {
                            try {
                                ObjectName elem = ((ObjectInstance) it.next()).getObjectName();
                                String name = elem.getKeyProperty("name"); // NOI18N
                                String url = "http://" + dm.getHost() + ":" + dm.getPort(); // NOI18N
                                String context = null;

                                if (name.endsWith(".war")) { // NOI18N
                                    name = name.substring(0, name.lastIndexOf(".war")); // NOI18N
                                }

                                if (abilitiesSupport.isRemoteManagementSupported()
                                        && (abilitiesSupport.isJB4x() || abilitiesSupport.isJB6x())) {
                                    if (SYSTEM_WEB_APPLICATIONS.contains(name)) { // Excluding it. It's system package
                                        continue;
                                    }
                                    String descr = (String) Util.getMBeanParameter(connection, "jbossWebDeploymentDescriptor", elem.getCanonicalName()); // NOI18N
                                    context = Util.getWebContextRoot(descr, name);
                                } else {
                                    if (name.startsWith("//localhost/")) { // NOI18N
                                        name = name.substring("//localhost/".length()); // NOI18N
                                    }
                                    if ("".equals(name)) {
                                        name = "ROOT"; // NOI18N // consistent with JBoss4
                                    }
                                    if (SYSTEM_WEB_APPLICATIONS.contains(name)) { // Excluding it. It's system package
                                        continue;
                                    }

                                    context = (String) Util.getMBeanParameter(connection, "path", elem.getCanonicalName()); // NOI18N
                                }

                                name += ".war"; // NOI18N
                                keys.add(new JBWebModuleNode(name, lookup, (context == null ? null : url + context)));
                            } catch (Exception ex) {
                                LOGGER.log(Level.INFO, null, ex);
                            }
                        }
                        return null;
                    }
                });

            } catch (ExecutionException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }

            setKeys(keys);
        }
    }

    class JBoss7WebNodeUpdater implements Runnable {

        List keys = new ArrayList();

        @Override
        public void run() {

            try {
                final JBDeploymentManager dm = (JBDeploymentManager) lookup.lookup(JBDeploymentManager.class);
                dm.invokeLocalAction(new Callable<Void>() {

                    @Override
                    public Void call() {
                        try {
                            Target[] targets = dm.getTargets();
                            ModuleType moduleType = ModuleType.WAR;

                            //Get all deployed WAR files.
                            TargetModuleID[] modules = dm.getAvailableModules(moduleType, targets);
                            // Module list may be null if nothing is deployed.
                            if (modules != null) {
                                //String url = "http://" + dm.getHost() + ":" + dm.getPort();
                                for (int intModule = 0; intModule < modules.length; intModule++) {
                                    String name = modules[intModule].getModuleID();
                                    if (name.endsWith(".war")) { // NOI18N
                                        name = name.substring(0, name.lastIndexOf(".war")); // NOI18N
                                    }
                                    if ("".equals(name)) { // NOI18N
                                        name = "ROOT"; // NOI18N // consistent with JBoss4
                                    }
                                    if (SYSTEM_WEB_APPLICATIONS.contains(name)) { // Excluding it. It's system package
                                        continue;
                                    }
                                    name += ".war"; // NOI18N
                                    keys.add(new JBWebModuleNode(name, lookup,  null));
                                }
                            }
                        } catch (TargetException ex) {
                            LOGGER.log(Level.INFO, null, ex);
                        } catch (IllegalStateException ex) {
                            LOGGER.log(Level.INFO, null, ex);
                        }
                        return null;
                    }
                });
            } catch (Exception ex) {
                LOGGER.log(Level.INFO, null, ex);
            }

            setKeys(keys);
        }
    }

    protected void addNotify() {
        updateKeys();
    }

    protected void removeNotify() {
        setKeys(java.util.Collections.EMPTY_SET);
    }

    protected org.openide.nodes.Node[] createNodes(Object key) {
        if (key instanceof JBWebModuleNode){
            return new Node[]{(JBWebModuleNode)key};
        }

        if (key instanceof String && key.equals(Util.WAIT_NODE)){
            return new Node[]{Util.createWaitNode()};
        }

        return null;
    }

}
