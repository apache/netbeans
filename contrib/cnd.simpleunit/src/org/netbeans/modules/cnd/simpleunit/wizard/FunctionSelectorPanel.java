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
package org.netbeans.modules.cnd.simpleunit.wizard;

import org.netbeans.modules.cnd.modelutil.ui.CheckTreeView;
import java.awt.BorderLayout;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.modelutil.ui.ElementNode;
import org.netbeans.modules.cnd.modelutil.ui.ElementNode.Description;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 */
public final class FunctionSelectorPanel extends JPanel implements ExplorerManager.Provider {

    private final ExplorerManager manager = new ExplorerManager();
    private final CheckTreeView elementView;
    /** Creates new form FunctionSelectorPanel */
    public FunctionSelectorPanel(boolean singleSelection) {
        setLayout(new BorderLayout());
        elementView = new CheckTreeView();
        add(elementView, BorderLayout.CENTER);
    }

    public void initFromElement(Description elementDescription, boolean singleSelection) {
        elementView.setRootVisible(false);
        setRootElement(elementDescription, singleSelection);
        //make sure that the first element is pre-selected
        Node root = manager.getRootContext();
        Node[] children = root.getChildren().getNodes();
        if (null != children && children.length > 0) {
            try {
                manager.setSelectedNodes(new org.openide.nodes.Node[]{children[0]});
            } catch (PropertyVetoException ex) {
                //ignore
            }
        }
    }

    public List<CsmDeclaration> getTreeSelectedElements() {
        ArrayList<CsmDeclaration> handles = new ArrayList<CsmDeclaration>();

        for (Node node : manager.getSelectedNodes()) {
            if (node instanceof ElementNode) {
                ElementNode.Description description = node.getLookup().lookup(ElementNode.Description.class);
                handles.add(description.getElementHandle());
            }
        }

        return handles;
    }

    public List<CsmDeclaration> getSelectedElements() {
        ArrayList<CsmDeclaration> handles = new ArrayList<CsmDeclaration>();

        Node n = manager.getRootContext();
        ElementNode.Description description = n.getLookup().lookup(ElementNode.Description.class);
        getSelectedHandles(description, handles);

        return handles;
    }

    public final void setRootElement(ElementNode.Description elementDescription, boolean singleSelection) {

        Node n;
        if (elementDescription != null) {
            ElementNode en = new ElementNode(elementDescription);
            en.setSingleSelection(singleSelection);
            n = en;
        } else {
            n = Node.EMPTY;
        }
        manager.setRootContext(n);

    }

    public void doInitialExpansion(int howMuch) {

        Node root = getExplorerManager().getRootContext();
        Node[] subNodes = root.getChildren().getNodes(true);

        if (subNodes == null) {
            return;
        }
        Node toSelect = null;

        int row = 0;

        boolean oldScroll = elementView.getScrollsOnExpand();
        elementView.setScrollsOnExpand(false);

        for (int i = 0; subNodes != null && i < (howMuch == - 1 || howMuch > subNodes.length ? subNodes.length : howMuch); i++) {
            // elementView.expandNode2(subNodes[i]);
            row++;
            elementView.expandRow(row);
            Node[] ssn = subNodes[i].getChildren().getNodes(true);
            row += ssn.length;
            if (toSelect == null) {
                if (ssn.length > 0) {
                    toSelect = getSelectedNode(ssn);
                }
            }
        }

        elementView.setScrollsOnExpand(oldScroll);

        try {
            if (toSelect != null) {
                getExplorerManager().setSelectedNodes(new org.openide.nodes.Node[]{toSelect});
            }
        } catch (PropertyVetoException ex) {
            // Ignore
        }
    }

    // ExplorerManager.Provider imlementation ----------------------------------
    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    private void getSelectedHandles(ElementNode.Description description,
            ArrayList<CsmDeclaration> target) {

        //#143049
        if (description == null) {
            return;
        }

        List<ElementNode.Description> subs = description.getSubs();

        if (subs == null) {
            return;
        }

        for (ElementNode.Description d : subs) {
            if (d.isSelectable() && d.isSelected()) {
                target.add(d.getElementHandle());
            } else {
                getSelectedHandles(d, target);
            }
        }
    }

    private Node getSelectedNode(Node[] children) {
        assert children.length > 0 : "array must have elements";
        for (Node node : children) {
            Description descr = node.getLookup().lookup(ElementNode.Description.class);
            if (descr != null && descr.isSelected()) {
                return node;
            }
        }
        return children[0];
    }

    void showLoadingNode() {
        elementView.setRootVisible(true);
        manager.setRootContext(getWaitNode());
    }

    private static Node WAIT_NODE;

    static synchronized Node getWaitNode() {
        if (WAIT_NODE == null) {
            WAIT_NODE = ElementNode.getWaitNode(NbBundle.getMessage(FunctionSelectorPanel.class, "LBL_WaitNode"));
        }
        return WAIT_NODE;
    }
}
