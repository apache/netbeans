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

package org.netbeans.modules.j2ee.ejbjar.project.ui;

import java.io.IOException;
import java.util.HashMap;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbNodesFactory;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.WeakListeners;

/**
 * Ejbs contained within a module
 * 
 * @author Chris Webster
 * @author Martin Adamek
 */
public class EjbContainerChildren extends Children.Keys<EjbContainerChildren.Key> implements PropertyChangeListener {

    private final EjbJar ejbModule;
    private final EjbNodesFactory nodeFactory;
    private final Project project;
    private final java.util.Map<Key, Node> nodesHash = Collections.synchronizedMap(new HashMap<Key, Node>());

    private Task updateTask = null;
    private static final RequestProcessor rp = new RequestProcessor();
    private AtomicBoolean listenerInitialized = new AtomicBoolean();

    public EjbContainerChildren(org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule, EjbNodesFactory nodeFactory, Project project) {
        this.ejbModule = ejbModule;
        this.nodeFactory = nodeFactory;
        this.project = project;
    }

    @Override
    protected void addNotify() {
        super.addNotify();
        setKeys(new Key[]{Key.SCANNING});
        updateKeys();
    }

    private synchronized void updateKeys(){
        if (updateTask != null){
            updateTask.schedule(100);
            return;
        }
        updateTask = rp.post(new Runnable(){
            public void run() {
                try {
                    Future<List<Key>> future = ejbModule.getMetadataModel().runReadActionWhenReady(new MetadataModelAction<EjbJarMetadata, List<Key>>() {

                        public List<Key> run(EjbJarMetadata metadata) throws Exception {
                            EnterpriseBeans beans = metadata.getRoot().getEnterpriseBeans();
                            if (beans != null) {
                                if (listenerInitialized.compareAndSet(false, true)) {
                                    beans.addPropertyChangeListener(WeakListeners.propertyChange(EjbContainerChildren.this, beans));
                                }
                                Key[] sessionBeans = Key.createArray(beans.getSession());
                                Key[] entityBeans = Key.createArray(beans.getEntity());
                                Key[] messageBeans = Key.createArray(beans.getMessageDriven());
                                Comparator<Key> ejbComparator = new Comparator<Key>() {

                                    public int compare(Key key1, Key key2) {
                                        return getEjbDisplayName(key1).compareTo(getEjbDisplayName(key2));
                                    }

                                    private String getEjbDisplayName(Key ejb) {
                                        String name = ejb.defaultDisplayName;
                                        if (name == null) {
                                            name = ejb.ejbName;
                                        }
                                        if (name == null) {
                                            name = "";
                                        }
                                        return name;
                                    }
                                };
                                Arrays.sort(sessionBeans, ejbComparator);
                                Arrays.sort(entityBeans, ejbComparator);
                                Arrays.sort(messageBeans, ejbComparator);
                                List<Key> keys = new ArrayList<Key>(sessionBeans.length + entityBeans.length + messageBeans.length);
                                keys.addAll(Arrays.asList(sessionBeans));
                                keys.addAll(Arrays.asList(messageBeans));
                                keys.addAll(Arrays.asList(entityBeans));
                                return keys;
                            }
                            return Collections.<Key>emptyList();
                        }
                    });
                    final List<Key> result = new ArrayList<Key>();
                    try {
                        result.addAll(future.get());
                    } catch (InterruptedException ie) {
                        Exceptions.printStackTrace(ie);
                    } catch (ExecutionException ee) {
                        Exceptions.printStackTrace(ee);
                    }
                    createNodesForKeys(result);
                    setKeys(result);
                } catch (MetadataModelException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }, 100);
    }

    private void createNodesForKeys(List<Key> keys){
        nodesHash.clear();
        for(Key key: keys){
            createNodes(key);
        }
    }

    @Override
    protected void removeNotify() {
        nodesHash.clear();
        super.removeNotify();
    }

    protected Node[] createNodes(Key key) {
        Node node = nodesHash.get(key);
        if (!nodesHash.containsKey(key)){
            node = null;
            if (key.ejbType == Key.EjbType.SESSION) {
                // do not create node for web service
                if (!key.isWebService && nodeFactory != null) {
                    node = nodeFactory.createSessionNode(key.ejbClass, ejbModule, project);
                }
            }
            if (key.ejbType == Key.EjbType.ENTITY && nodeFactory != null) {
                node = nodeFactory.createEntityNode(key.ejbClass, ejbModule, project);
            }
            if (key.ejbType == Key.EjbType.MESSAGE_DRIVEN && nodeFactory != null) {
                node = nodeFactory.createMessageNode(key.ejbClass, ejbModule, project);
            }
            if (key == Key.SCANNING){
                node = new AbstractNode(Children.LEAF);
                node.setDisplayName(NbBundle.getMessage(EjbContainerChildren.class, "MSG_Scanning_EJBs")); //NOI18N
                ((AbstractNode)node).setIconBaseWithExtension("org/netbeans/modules/j2ee/ejbjar/project/ui/wait.gif"); //NOI18N
            }
            nodesHash.put(key, node);
        }
        return node == null ? null : new Node[] { node };
    }

    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        updateKeys();
    }

    static final class Key {
        public static final Key SCANNING = new Key(null, null, null, null, false);

        private enum EjbType { SESSION, ENTITY, MESSAGE_DRIVEN }
        
        private final EjbType ejbType;
        private final String ejbClass;
        private final String defaultDisplayName;
        private final String ejbName;
        private final boolean isWebService;
        
        private Key(EjbType ejbType, String ejbClass, String defaultDisplayName, String ejbName, boolean  isWebService) {
            this.ejbType = ejbType;
            this.ejbClass = ejbClass;
            this.defaultDisplayName = defaultDisplayName;
            this.ejbName = ejbName;
            this.isWebService = isWebService;
        }
        
        public static Key[] createArray(Ejb[] ejbs) {
            Key[] keys = new Key[ejbs.length];
            for (int i = 0; i < ejbs.length; i++) {
                Ejb ejb = ejbs[i];
                EjbType ejbType = null;
                boolean isWebService = false;
                if (ejb instanceof Session) {
                    ejbType = EjbType.SESSION;
                    try {
                        isWebService = ((Session) ejb).getServiceEndpoint() != null;
                    } catch (VersionNotSupportedException ex) {
                        // not supported for J2EE 1.3
                    }
                } else if (ejb instanceof Entity) {
                    ejbType = EjbType.ENTITY;
                } else if (ejb instanceof MessageDriven) {
                    ejbType = EjbType.MESSAGE_DRIVEN;
                }
                keys[i] = new Key(
                        ejbType,
                        ejb.getEjbClass(),
                        ejb.getDefaultDisplayName(),
                        ejb.getEjbName(),
                        isWebService
                        );
            }
            return keys;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Key other = (Key) obj;
            if (this.ejbClass != other.ejbClass &&
               (this.ejbClass == null || !this.ejbClass.equals(other.ejbClass))) {
                return false;
            }

            if (this.defaultDisplayName != other.defaultDisplayName &&
               (this.defaultDisplayName == null || !this.defaultDisplayName.equals(other.defaultDisplayName))) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + (this.ejbClass != null ? this.ejbClass.hashCode() : 0);
            return hash;
        }

        
    }
    
}
