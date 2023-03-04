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

package org.netbeans.core.ide;

import java.awt.BorderLayout;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.List;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.text.DefaultEditorKit;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.TreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Handle;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Services tab which lists nodes found in {@code UI/Runtime}.
 */
@TopComponent.Description(preferredID=ServicesTab.ID, iconBase="org/netbeans/core/ide/resources/services.gif", persistenceType=TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode="explorer", position=500, openAtStartup=false)
@ActionID(category="Window", id="org.netbeans.core.ide.ServicesTabAction")
@ActionReference(path="Menu/Window", position=500)
@TopComponent.OpenActionRegistration(displayName="#CTL_ServicesTabAction", preferredID=ServicesTab.ID)
public class ServicesTab extends TopComponent implements ExplorerManager.Provider {

    private static final long serialVersionUID = 1L;

    private final ExplorerManager manager;
    private final TreeView view;

    private ServicesTab() {
        manager = new ExplorerManager();
        manager.setRootContext(new ServicesNode());
        ActionMap map = getActionMap();
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
        map.put("delete", ExplorerUtils.actionDelete(manager, false));
        associateLookup(ExplorerUtils.createLookup(manager, map));
        view = new BeanTreeView();
        view.setRootVisible(false);
        setLayout(new BorderLayout());
        add(view);
        setName(ID);
        setDisplayName(NbBundle.getMessage(ServicesTab.class, "LBL_Services"));
    }

    static final String ID = "services"; // NOI18N

    public ExplorerManager getExplorerManager() {
        return manager;
    }
    protected @Override void componentActivated() {
        ExplorerUtils.activateActions(manager, true);
    }
    protected @Override void componentDeactivated() {
        ExplorerUtils.activateActions(manager, false);
    }

    @Override
    protected void componentShowing () {
        super.componentShowing();
        setDisplayName(NbBundle.getMessage(ServicesTab.class, "LBL_Services")); //NOI18N
    }

    public @Override boolean requestFocusInWindow() {
        super.requestFocusInWindow();
        return view.requestFocusInWindow();
    }

    public @Override HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.core.actions.ViewRuntimeTabAction"); // NOI18N
    }

    static class ServicesNode extends AbstractNode {

        ServicesNode() {
            super(Children.create(new ServicesChildren(), true));
        }

        public @Override PasteType getDropType(Transferable t, int action, int index) {
            return null; // #118628
        }

        public @Override Handle getHandle() {
            return new Handle() {
                public @Override Node getNode() throws IOException {
                    TopComponent tc = WindowManager.getDefault().findTopComponent(ID);
                    if (tc instanceof ServicesTab) {
                        return ((ServicesTab) tc).manager.getRootContext();
                    } else {
                        throw new IOException("no ServicesTab");
                    }
                }
            };
        }

        public @Override Action[] getActions(boolean context) {
            return new Action[0];
        }

        private static class ServicesChildren extends ChildFactory<Node> implements LookupListener {

            private final Lookup.Result<Node> nodes = Lookups.forPath("UI/Runtime").lookupResult(Node.class);
            ServicesChildren() {
                nodes.addLookupListener(WeakListeners.create(LookupListener.class, this, nodes));
            }

            protected @Override Node createNodeForKey(Node key) {
                return key.cloneNode();
            }

            protected boolean createKeys(List<Node> toPopulate) {
                toPopulate.addAll(nodes.allInstances()); // NOI18N
                return true;
            }

            public void resultChanged(LookupEvent ev) {
                refresh(false);
            }

        }

    }

}
