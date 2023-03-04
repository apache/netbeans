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

package org.netbeans.modules.navigator;

import java.beans.PropertyVetoException;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.text.DefaultEditorKit;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.ListView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/** Example of Explorer View integration into NavigatorPanel, with working
 * explorer actions. Nodes selected in explorer view, managed by explorer
 * manager, are propagated to global activated nodes.
 * 
 * Key points for integration are: <br></br>
 * - return lookup created using ExplorerUtils.createLookup(...) from getLookup() method
 * - use ExplorerUtils.activateActions(..) for actions activation in panelActivated
 *
 * @author Dafe Simonek
 */
public class ListViewNavigatorPanel extends JPanel implements NavigatorPanel, ExplorerManager.Provider {
    
    private ExplorerManager manager;
    private ListView listView;
    private Lookup lookup;
    private Action copyAction;
    
    public ListViewNavigatorPanel () {
        manager = new ExplorerManager();
        ActionMap map = getActionMap();
        copyAction = ExplorerUtils.actionCopy(manager);
        map.put(DefaultEditorKit.copyAction, copyAction);
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
        map.put("delete", ExplorerUtils.actionDelete(manager, true)); // or false

        lookup = ExplorerUtils.createLookup(manager, map);

        listView = new ListView();
        fillListView(listView);
                
        add(listView);
    }

    public String getDisplayName() {
        return "List view panel";
    }

    public String getDisplayHint() {
        return "List view based navigator panel";
    }

    public JComponent getComponent() {
        return this;
    }

    public void panelActivated(Lookup context) {
        ExplorerUtils.activateActions(manager, true);
    }

    public void panelDeactivated() {
        ExplorerUtils.activateActions(manager, false);
    }

    public Lookup getLookup() {
        return lookup;
    }

    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    public Action getCopyAction () {
        return copyAction;
    }

    private void fillListView(ListView listView) {
        try {
            Node testNode = new AbstractNode(Children.LEAF);
            manager.setRootContext(testNode);
            manager.setSelectedNodes(new Node[]{testNode});
        } catch (PropertyVetoException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
}
