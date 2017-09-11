/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.team.commons.treelist;

import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.SwingUtilities;

/**
 * List model which allows adding and removing of nodes to simulate tree-like
 * expand/collapse actions.
 *
 * @author S. Aubrecht
 */
public class TreeListModel extends AbstractListModel implements TreeListListener {

    private final ArrayList<TreeListNode> nodes = new ArrayList<TreeListNode>(500);
    private List<TreeListModelListener> modelListeners = new ArrayList<TreeListModelListener>();

    public int getSize() {
        synchronized (nodes) {
            return nodes.size();
        }
    }

    public Object getElementAt(int index) {
        synchronized (nodes) {
            if (index < 0 || index >= nodes.size()) {
                return null;
            }
            return nodes.get(index);
        }
    }

    /**
     * Add given node at root position. If the node is expanded then its
     * children are also added to the model.
     *
     * @param rootIndex Index into the list of all root nodes (excluding non-root
     * ones).
     * @param root Node to be added
     */
    public void addRoot(int rootIndex, TreeListNode root) {
        int firstIndex = -1;
        int lastIndex = -1;
        synchronized (nodes) {
            int index = getAllNodesIndex(rootIndex);
            if (index < 0 || index >= nodes.size()) {
                nodes.add(root);
            } else {
                nodes.add(index, root);
            }
            int parentIndex = nodes.indexOf(root);
            firstIndex = parentIndex;
            root.setListener(this);
            root.attach();
            if (root.isExpanded()) {
                if (!root.getChildren().isEmpty()) {
                    lastIndex = addNodes(parentIndex + 1, root.getChildren()) - 1;
                } else {
                    root.setExpanded(true);
                }
            }
        }
        if (firstIndex >= 0) {
            if (lastIndex < firstIndex) {
                lastIndex = firstIndex;
            }
            fireIntervalAdded(this, firstIndex, lastIndex);
        }
    }

    /**
     * Remove given node from the model, also removes all its children.
     *
     * @param root
     */
    public void removeRoot(TreeListNode root) {
        int firstIndex = -1;
        int lastIndex = -1;
        synchronized (nodes) {
            firstIndex = nodes.indexOf(root);
            if (firstIndex < 0) {
                root.dispose();
                return;
            }
            ArrayList<TreeListNode> toRemove = findDescendants(root);
            if (null == toRemove) {
                toRemove = new ArrayList<TreeListNode>(1);
            }
            toRemove.add(0, root);
            lastIndex = nodes.indexOf(toRemove.get(toRemove.size() - 1));
            for (TreeListNode node : toRemove) {
                node.dispose();
            }
            nodes.removeAll(toRemove);
        }
        if (firstIndex >= 0) {
            fireIntervalRemoved(this, firstIndex, lastIndex);
        }
    }

    /**
     * Remove all nodes from the model.
     */
    public void clear() {
        int lastIndex = -1;
        synchronized (nodes) {
            lastIndex = nodes.size() - 1;
            for (TreeListNode node : nodes) {
                node.dispose();
            }
            nodes.clear();
        }
        fireIntervalRemoved(this, 0, lastIndex);
    }

    /**
     * @return List of root nodes.
     */
    public List<TreeListNode> getRootNodes() {
        ArrayList<TreeListNode> res = new ArrayList<TreeListNode>(getSize());
        synchronized (nodes) {
            for (TreeListNode n : nodes) {
                if (null == n.getParent()) {
                    res.add(n);
                }
            }
        }
        return res;
    }

    /**
     * @return List of all nodes.
     */
    public List<TreeListNode> getAllNodes() {
        synchronized (nodes) {
            ArrayList<TreeListNode> res = new ArrayList<TreeListNode>(nodes);
            return res;
        }
    }

    private void removeChildrenOf(TreeListNode parent) {
        int firstIndex = -1;
        int lastIndex = -1;
        synchronized (nodes) {
            ArrayList<TreeListNode> toRemove = findDescendants(parent);
            if (null == toRemove) {
                return;
            }
            firstIndex = nodes.indexOf(toRemove.get(0));
            lastIndex = nodes.indexOf(toRemove.get(toRemove.size() - 1));
            for (TreeListNode node : toRemove) {
                node.dispose();
            }
            nodes.removeAll(toRemove);
        }
        if (firstIndex >= 0) {
            fireIntervalRemoved(this, firstIndex, lastIndex);
        }
    }

    private ArrayList<TreeListNode> findDescendants(TreeListNode parent) {
        ArrayList<TreeListNode> descendants = null;
        int parentIndex = nodes.indexOf(parent);
        if (parentIndex < 0) {
            return null; //nothing to remove
        }
        for (int i = parentIndex + 1; i < nodes.size(); i++) {
            TreeListNode child = nodes.get(i);
            if (child.isDescendantOf(parent)) {
                if (null == descendants) {
                    descendants = new ArrayList<TreeListNode>(20);
                }
                descendants.add(child);
            } else {
                break;
            }
        }
        return descendants;
    }

    private void addChildrenOf(TreeListNode parent) {
        int firstIndex = -1;
        int lastIndex = -1;
        List<TreeListNode> children = parent.getChildren();
        if (children == null || children.isEmpty()) {
            return;
        }
        synchronized (nodes) {
            firstIndex = nodes.indexOf(parent);
            if (firstIndex < 0) {
                return; //the parent isn't visible in the model, so don't bother
            }
            firstIndex++;
            lastIndex = addNodes(firstIndex, children) - 1;
        }
        if (firstIndex >= 0) {
            if (lastIndex < firstIndex) {
                lastIndex = firstIndex;
            }
            fireIntervalAdded(this, firstIndex, lastIndex);
            fireNodeExpanded(parent);
        }
    }

    private int addNodes(int insertPoint, List<TreeListNode> newNodes) {
        for (int i = 0; i < newNodes.size(); i++) {
            TreeListNode node = newNodes.get(i);
            if (nodes.contains(node)) {
                continue;
            }
            node.setListener(this);
            node.attach();
            nodes.add(insertPoint++, node);
            if (node.isExpanded()) {
                if (!node.getChildren().isEmpty()) {
                    insertPoint = addNodes(insertPoint, node.getChildren());
                } else {
                    node.setExpanded(true);
                }

            }
        }
        return insertPoint;
    }

    @Override
    public void childrenRemoved(TreeListNode parent) {
        removeChildrenOf(parent);
    }

    @Override
    public void childrenAdded(TreeListNode parent) {
        addChildrenOf(parent);
    }

    @Override
    public void contentChanged(ListNode node) {
        int index = -1;
        synchronized (nodes) {
            index = nodes.indexOf(node);
        }
        if (index >= 0) {
            fireContentsChanged(this, index, index);
        }
    }

    @Override
    public void contentSizeChanged(ListNode node) { }
    
    @Override
    protected void fireContentsChanged(final Object source, final int index0, final int index1) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    TreeListModel.super.fireContentsChanged(source, index0, index1);
                }
            });
        } else {
            super.fireContentsChanged(source, index0, index1);
        }
    }

    @Override
    protected void fireIntervalAdded(final Object source, final int index0, final int index1) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    TreeListModel.super.fireIntervalAdded(source, index0, index1);
                }
            });
        } else {
            super.fireIntervalAdded(source, index0, index1);
        }
    }

    @Override
    protected void fireIntervalRemoved(final Object source, final int index0, final int index1) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    TreeListModel.super.fireIntervalRemoved(source, index0, index1);
                }
            });
        } else {
            super.fireIntervalRemoved(source, index0, index1);
        }
    }

    private int getAllNodesIndex(int rootIndex) {
        List<TreeListNode> rootNodes = getRootNodes();
        if (rootIndex < 0 || rootIndex >= rootNodes.size()) {
                return -1;
            } else {
                TreeListNode rootNode = rootNodes.get(rootIndex);
                int index = nodes.indexOf(rootNode);
                return index;
            }
    }

    public void addModelListener(TreeListModelListener listener){
        modelListeners.add(listener);
    }

    public void removeModelListener(TreeListModelListener listener){
        modelListeners.remove(listener);
    }

    private void fireNodeExpanded(TreeListNode node){
        for (TreeListModelListener listener : modelListeners) {
            listener.nodeExpanded(node);
        }
    }
}
