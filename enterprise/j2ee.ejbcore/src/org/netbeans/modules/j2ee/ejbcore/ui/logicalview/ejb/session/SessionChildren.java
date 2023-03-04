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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.session;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.SessionMethodController;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.EjbViewController;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.MethodsNode;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 * @author Chris Webster
 * @author Martin Adamek
 */

public final class SessionChildren extends Children.Keys<SessionChildren.Key> implements PropertyChangeListener {
    
    // indexes into fields with results for model query
    private static final int REMOTE = 0;
    private static final int LOCAL = 1;
    private static final int BEAN = 2;

    public enum Key {REMOTE, LOCAL, BEAN};
    
    private final ClasspathInfo cpInfo;
    private final String ejbClass;
    private final EjbJar ejbModule;
    private final SessionMethodController controller;
    private static final RequestProcessor RP = new RequestProcessor(SessionChildren.class.getName(), 5);

    public SessionChildren(EjbViewController ejbViewController) {
        this.cpInfo = ejbViewController.getClasspathInfo();
        this.ejbClass = ejbViewController.getEjbClass();
        this.ejbModule = ejbViewController.getEjbModule();
        controller = new SessionMethodController(ejbClass, ejbModule.getMetadataModel());
    }
    
    @Override
    protected void addNotify() {
        super.addNotify();
        try {
            updateKeys();
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }
    
    private void updateKeys() throws IOException {
        RP.submit(new Runnable() {
            @Override
            public void run() {
                final boolean[] results = new boolean[]{false, false, true};
                try {
                    ejbModule.getMetadataModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
                        @Override
                        public Void run(EjbJarMetadata metadata) throws Exception {
                            Session entity = (Session) metadata.findByEjbClass(ejbClass);
                            if (entity != null) {
                                results[REMOTE] = entity.getRemote() != null;
                                results[LOCAL] = entity.getLocal() != null;
                                entity.removePropertyChangeListener(WeakListeners.propertyChange(SessionChildren.this, entity));
                                entity.addPropertyChangeListener(WeakListeners.propertyChange(SessionChildren.this, entity));
                            }
                            return null;
                        }
                    });
                    controller.refresh();
                } catch (MetadataModelException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        List<Key> keys = new ArrayList<Key>();
                        if (results[REMOTE]) { keys.add(Key.REMOTE); }
                        if (results[LOCAL]) { keys.add(Key.LOCAL); }
                        if (results[BEAN]) { keys.add(Key.BEAN); }
                        setKeys(keys);
                    }
                });
            }
        });
    }

    @Override
    protected void removeNotify() {
        setKeys(Collections.<Key>emptyList());
        super.removeNotify();
    }
    
    @Override
    protected Node[] createNodes(Key key) {
        if (Key.LOCAL == key) {
            MethodChildren children = new MethodChildren(this, cpInfo, ejbModule, controller, MethodsNode.ViewType.LOCAL);
            MethodsNode n = new MethodsNode(ejbClass, ejbModule, children, MethodsNode.ViewType.LOCAL);
            n.setIconBaseWithExtension("org/netbeans/modules/j2ee/ejbcore/resources/LocalMethodContainerIcon.gif");
            n.setDisplayName(NbBundle.getMessage(EjbViewController.class, "LBL_LocalMethods"));
            return new Node[] { n };
        }
        if (Key.REMOTE == key) {
            MethodChildren children = new MethodChildren(this, cpInfo, ejbModule, controller, MethodsNode.ViewType.REMOTE);
            MethodsNode n = new MethodsNode(ejbClass, ejbModule, children, MethodsNode.ViewType.REMOTE);
            n.setIconBaseWithExtension("org/netbeans/modules/j2ee/ejbcore/resources/RemoteMethodContainerIcon.gif");
            n.setDisplayName(NbBundle.getMessage(EjbViewController.class, "LBL_RemoteMethods"));
            return new Node[] { n };
        }
        if (Key.BEAN == key) {
            MethodChildren children = new MethodChildren(this, cpInfo, ejbModule, controller, MethodsNode.ViewType.NO_INTERFACE);
            MethodsNode n = new MethodsNode(ejbClass, ejbModule, children, MethodsNode.ViewType.NO_INTERFACE);
            n.setIconBaseWithExtension("org/netbeans/modules/j2ee/ejbcore/resources/MethodContainerIcon.gif");
            n.setDisplayName(NbBundle.getMessage(EjbViewController.class, "LBL_BeanMethods"));
            return new Node[] { n };
        }
        return null;
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        String prop = propertyChangeEvent.getPropertyName();
        if (Session.BUSINESS_LOCAL.equals(prop) || Session.BUSINESS_REMOTE.equals(prop)
                || MethodChildren.TYPE_CHANGE.equals(prop)) {
            try {
                updateKeys();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
}
