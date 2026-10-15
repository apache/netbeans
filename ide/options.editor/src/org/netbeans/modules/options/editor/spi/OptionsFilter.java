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

package org.netbeans.modules.options.editor.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.spi.options.OptionsPanelController;

/**Allows to filter a tree based on user's input. Folder based {@link OptionsPanelController}s
 * that support filtering will put an instance of {@link TreeModelFilter} to the
 * lookup passed to the controller.
 *
 * @author Jan Lahoda
 * @since 1.19
 */
public final class OptionsFilter {

    private final Document doc;
    private final Runnable usedCallback;

    private OptionsFilter(Document doc, Runnable usedCallback) {
        this.doc = doc;
        this.usedCallback = usedCallback;
    }

    /**Install a filtering model to the given tree, using given model as the source
     * of the data.
     *
     * @param tree to which the model should be installed
     * @param source source {@link TreeModel} - the data to show will be gathered from this model
     * @param acceptor acceptor specifying whether the given original tree node should or should not
     *                 be visible for given user's filter
     * @param delegatingTreeModelListeners is allowing to delegate tree model listeners to tree 
     */
    public void installFilteringModel(JTree tree, TreeModel source, Acceptor acceptor, boolean delegatingTreeModelListeners) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Not in AWT Event Dispatch Thread");
        }
        
        usedCallback.run();
        tree.setModel(new FilteringTreeModel(source, doc, acceptor, delegatingTreeModelListeners));
    }
    
    public void installFilteringModel(JTree tree, TreeModel source, Acceptor acceptor) {
        installFilteringModel(tree, source, acceptor, false);
    }

    public interface Acceptor {
        public boolean accept(Object originalTreeNode, String filterText);
    }

    private static final class FilteringTreeModel implements TreeModel, TreeModelListener, DocumentListener {

        private final TreeModel delegate;
        private final Document filter;
        private final Acceptor acceptor;
        private final Map<Object, List<Object>> category2Nodes = new HashMap<Object, List<Object>>();
        private final boolean delegatingTreeModelListener;

        public FilteringTreeModel(TreeModel delegate, Document filter, Acceptor acceptor, boolean delegatingTreeModelListeners) {
            this.delegate = delegate;
            this.filter = filter;
            this.acceptor = acceptor;
            this.delegatingTreeModelListener = delegatingTreeModelListeners;

            this.delegate.addTreeModelListener(this);
            this.filter.addDocumentListener(this);

            filter();
        }

        @Override
        public Object getRoot() {
            return delegate.getRoot();
        }

        @Override
        public Object getChild(Object parent, int index) {
            return category2Nodes.get(parent).get(index);
        }

        @Override
        public int getChildCount(Object parent) {
            return category2Nodes.get(parent).size();
        }

        @Override
        public boolean isLeaf(Object node) {
            return delegate.isLeaf(node);
        }

        @Override
        public void valueForPathChanged(TreePath path, Object newValue) {
            delegate.valueForPathChanged(path, newValue);
        }

        @Override
        public int getIndexOfChild(Object parent, Object child) {
            List<Object> catNodes = category2Nodes.get(parent);

            if (catNodes == null) return -1;
            
            return catNodes.indexOf(child);
        }

        private final List<TreeModelListener> listeners = new LinkedList<TreeModelListener>();

        @Override
        public synchronized void addTreeModelListener(TreeModelListener l) {
            listeners.add(l);
            if (delegatingTreeModelListener) {
                delegate.addTreeModelListener(l);
            }
        }

        @Override
        public synchronized void removeTreeModelListener(TreeModelListener l) {
            listeners.remove(l);
            if (delegatingTreeModelListener) {
                delegate.removeTreeModelListener(l);
            }
        }

        private synchronized Iterable<? extends TreeModelListener> getListeners() {
            return new LinkedList<TreeModelListener>(listeners);
        }

        void filter() {
            final String[] term = new String[1];

            filter.render(new Runnable() {
                public void run() {
                    try {
                        term[0] = filter.getText(0, filter.getLength());
                    } catch (BadLocationException ex) {
                        throw new IllegalStateException(ex);
                    }
                }
            });

            category2Nodes.clear();

            filterNodes(delegate.getRoot(), term[0]);

            for (TreeModelListener l : getListeners()) {
                l.treeStructureChanged(new TreeModelEvent(this, new Object[] {getRoot()}));
            }
        }
        
        private boolean filterNodes(Object currentNode, String term) {
            boolean accepted = term.isEmpty() || acceptor.accept(currentNode, term);
            
            if (delegate.isLeaf(currentNode)) {
                category2Nodes.put(currentNode, Collections.emptyList());
                return accepted;
            }
            
            List<Object> filtered = new ArrayList<Object>(delegate.getChildCount(currentNode));
            
            for (int c = 0; c < delegate.getChildCount(currentNode); c++) {
                Object child = delegate.getChild(currentNode, c);

                if (filterNodes(child, term)) {
                    filtered.add(child);
                    accepted |= true;
                }
            }

            if (term.isEmpty() || accepted || currentNode == delegate.getRoot()) {
                category2Nodes.put(currentNode, filtered);
            }
            
            return accepted;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            filter();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            filter();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {}

        @Override
        public void treeNodesChanged(TreeModelEvent e) {
            if (   (e.getTreePath().getPathCount() > 1)
                && (getIndexOfChild(e.getTreePath().getParentPath().getLastPathComponent(), e.getTreePath().getLastPathComponent()) == (-1))) {
                //the category is filtered out - to point in firing events
                return ;
            }

            List<Integer> childIndices = new LinkedList<>();
            List<Object> children = new LinkedList<>();

            Object[] ch = e.getChildren();
            
            // special case for root node: include all children
            if (ch == null) {
                List<? extends TreeNode> l = Collections.list(((TreeNode)e.getTreePath().getLastPathComponent()).children());
                ch = l.toArray(new Object[0]);
            }
            for (Object c : ch) {
                int i = getIndexOfChild(e.getTreePath().getLastPathComponent(), e.getTreePath().getLastPathComponent());

                if (i == (-1)) continue;

                childIndices.add(i);
                children.add(c);
            }

            int[] childIndicesArray = new int[childIndices.size()];
            int o = 0;

            for (Integer i : childIndices) {
                childIndicesArray[o++] = i;
            }

            TreeModelEvent nue = new TreeModelEvent(this, e.getTreePath(), childIndicesArray, children.toArray(new Object[0]));
            
            for (TreeModelListener l : getListeners()) {
                l.treeNodesChanged(nue);
            }
        }

        @Override
        public void treeNodesInserted(TreeModelEvent e) {
        }

        @Override
        public void treeNodesRemoved(TreeModelEvent e) {
        }

        @Override
        public void treeStructureChanged(TreeModelEvent e) {
        }

    }

    /**Create the OptionsFilter.
     * 
     * @param doc from which the filtering text should be read
     * @param usedCallback callback that is invoked when the model is installed
     * @return a newly created OptionsFilter.
     * @since 1.42
     */
    public static OptionsFilter create(Document doc, Runnable usedCallback) {
        return new OptionsFilter(doc, usedCallback);
    }
    
}
