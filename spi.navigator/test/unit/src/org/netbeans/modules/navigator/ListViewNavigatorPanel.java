/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
