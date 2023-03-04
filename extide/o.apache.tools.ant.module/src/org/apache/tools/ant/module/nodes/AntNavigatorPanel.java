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

package org.apache.tools.ant.module.nodes;

import java.awt.BorderLayout;
import java.util.Collection;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import org.apache.tools.ant.module.loader.AntProjectDataObject;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.ListView;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 * Displays Ant targets in the Navigator.
 * @author Jesse Glick
 */
@NavigatorPanel.Registration(mimeType=AntProjectDataObject.MIME_TYPE, displayName="#ANP_label")
public final class AntNavigatorPanel implements NavigatorPanel {
    
    private Lookup.Result<DataObject> selection;
    private final LookupListener selectionListener = new LookupListener() {
        public void resultChanged(LookupEvent ev) {
            Mutex.EVENT.readAccess(new Runnable() { // #69355: safest to run in EQ
                public void run() {
                    if (selection != null) { // #111772
                        display(selection.allInstances());
                    }
                }
            });
        }
    };
    private JComponent panel;
    private final ExplorerManager manager = new ExplorerManager();
    private final Lookup lookup = ExplorerUtils.createLookup(manager, new ActionMap());
    
    /**
     * Default constructor for layer instance.
     */
    public AntNavigatorPanel() {}
    
    public String getDisplayName() {
        return NbBundle.getMessage(AntNavigatorPanel.class, "ANP_label");
    }
    
    public String getDisplayHint() {
        return NbBundle.getMessage(AntNavigatorPanel.class, "ANP_hint");
    }
    
    public JComponent getComponent() {
        if (panel == null) {
            final ListView view = new ListView();
            view.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            class Panel extends JPanel implements ExplorerManager.Provider, Lookup.Provider {
                // Make sure action context works correctly:
                {
                    setLayout(new BorderLayout());
                    add(view, BorderLayout.CENTER);
                }
                public ExplorerManager getExplorerManager() {
                    return manager;
                }
                @Override
                public boolean requestFocusInWindow() {
                    return view.requestFocusInWindow();
                }
                public Lookup getLookup() {
                    return lookup;
                }
            }
            panel = new Panel();
        }
        return panel;
    }
    
    public void panelActivated(Lookup context) {
        selection = context.lookupResult(DataObject.class);
        selection.addLookupListener(selectionListener);
        selectionListener.resultChanged(null);
    }
    
    public void panelDeactivated() {
        selection.removeLookupListener(selectionListener);
        selection = null;
    }
    
    public Lookup getLookup() {
        return lookup;
    }
    
    private void display(Collection<? extends DataObject> selectedFiles) {
        // Show list of targets for selected file:
        if (selectedFiles.size() == 1) {
            DataObject d = selectedFiles.iterator().next();
            if (d.isValid()) { // #145571
                manager.setRootContext(d.getNodeDelegate());
                return;
            }
        }
        // Fallback:
        manager.setRootContext(Node.EMPTY);
    }
    
}
