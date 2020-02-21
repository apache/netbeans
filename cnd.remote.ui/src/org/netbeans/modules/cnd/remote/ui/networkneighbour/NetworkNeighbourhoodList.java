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
package org.netbeans.modules.cnd.remote.ui.networkneighbour;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.dnd.DnDConstants;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.swing.outline.Outline;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.OutlineView;
import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;

/**
 *
 */
public final class NetworkNeighbourhoodList extends JPanel implements ExplorerManager.Provider {

    private static final NetworkRegistry registry = NetworkRegistry.getInstance();
    private final ExplorerManager mgr;
    private final NeighbourhoodRootNode rootNode;
    private final ChangeListener changeListener;

    @SuppressWarnings("deprecation")
    public NetworkNeighbourhoodList() {
        setLayout(new BorderLayout());
        mgr = new ExplorerManager();
        rootNode = new NeighbourhoodRootNode();
        mgr.setRootContext(rootNode);
        changeListener = new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                rootNode.refresh(registry.getHosts());
            }
        };

        OutlineView view = new OutlineView(
                NbBundle.getMessage(NetworkNeighbourhoodList.class,
                "NetworkNeighbourhoodList.ColumnHost.Title")); // NOI18N

        view.setAllowedDragActions(DnDConstants.ACTION_NONE);
        view.setAllowedDropActions(DnDConstants.ACTION_NONE);
        Outline outline = view.getOutline();
        outline.setRootVisible(false);
        outline.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        outline.setFullyNonEditable(true);
        view.setPopupAllowed(false);

        view.setProperties(new Property<?>[]{
                    new PropertySupport.ReadOnly<Image>(
                    NeighbourHostNode.PROP_ACCEPTS_SSH, Image.class,
                    NbBundle.getMessage(NetworkNeighbourhoodList.class,
                    "NetworkNeighbourhoodList.ColumnSSH.Title"), // NOI18N
                    NbBundle.getMessage(NetworkNeighbourhoodList.class,
                    "NetworkNeighbourhoodList.HostCannotbeConnected")) {//NOI18N

                        @Override
                        public Image getValue() throws IllegalAccessException, InvocationTargetException {
                            return null;
                        }
                    }
                });

        outline.getColumn(NbBundle.getMessage(NetworkNeighbourhoodList.class,
                "NetworkNeighbourhoodList.ColumnSSH.Title")).setMaxWidth(40);//NOI18N

        add(view, BorderLayout.CENTER);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        registry.addChangeListener(changeListener);
        registry.startScan();
    }

    @Override
    public void removeNotify() {
        registry.stopScan();
        registry.removeChangeListener(changeListener);
        super.removeNotify();
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return mgr;
    }
}
