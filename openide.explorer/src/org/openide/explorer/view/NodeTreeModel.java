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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.openide.explorer.view;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import org.openide.nodes.Node;
import org.openide.util.*;


import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;

import java.util.List;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.*;


/** Model for displaying the nodes in tree.
*
* @author Jaroslav Tulach
*/
public class NodeTreeModel extends DefaultTreeModel {
    static final long serialVersionUID = 1900670294524747212L;
    private static final Logger LOG = Logger.getLogger(NodeTreeModel.class.getName());

    /** listener used to listen to changes in trees */
    private transient Listener listener;

    // Workaround for JDK issue 6472844 (NB #84970)
    // second part is in the listener and third in the TreeView
    private CopyOnWriteArrayList<TreeView> views = new CopyOnWriteArrayList<TreeView>();
    void addView(TreeView tw) {
        views.add(tw);
    }
    
    /** Creates new NodeTreeModel
    */
    public NodeTreeModel() {
        super(VisualizerNode.EMPTY, true);
    }

    /** Creates new NodeTreeModel
    * @param root the root of the model
    */
    public NodeTreeModel(Node root) {
        super(VisualizerNode.EMPTY, true);
        doCallSetNode(root);
    }

    final void doCallSetNode(Node r) {
        setNode(r);
    }

    /** Changes the root of the model. This is thread safe method.
    * @param root the root of the model
    */
    public void setNode(final Node root) {
        setNode(root, null);
    }
    
    void setNode(final Node root, final TreeView.VisualizerHolder visHolder) {
        Mutex.EVENT.readAccess(
            new Runnable() {
            @Override
                public void run() {
                    VisualizerNode v = (VisualizerNode) getRoot();
                    VisualizerNode nr = VisualizerNode.getVisualizer(null, root);

                    if (v == nr) {
                        // no change
                        return;
                    }

                    v.removeNodeModel(listener());

                    nr.addNodeModel(listener());
                    setRoot(nr);
                    if (visHolder != null) {
                        visHolder.add(nr.getChildren());
                        visHolder.removeRecur(v.getChildren());
                    }
                }
            }
        );
    }

    /** Getter for the listener. Only from AWT-QUEUE.
    */
    private Listener listener() {
        if (listener == null) {
            listener = new Listener(this);
        }

        return listener;
    }

    /**
    * This sets the user object of the TreeNode identified by path
    * and posts a node changed.  If you use custom user objects in
    * the TreeModel you'returngoing to need to subclass this and
    * set the user object of the changed node to something meaningful.
    */
    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        if (path == null) {
            return;
        }

        Object o = path.getLastPathComponent();

        if (o instanceof VisualizerNode) {
            nodeChanged((VisualizerNode) o);

            return;
        }

        MutableTreeNode aNode = (MutableTreeNode) o;

        aNode.setUserObject(newValue);
        nodeChanged(aNode);
    }

    void nodesWereInsertedInternal(final VisualizerEvent ev) {
        if (listenerList == null) {
            return;
        }

        TreeNode node = ev.getVisualizer();
        Object[] path = getPathToRoot(node);

        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==TreeModelListener.class) {
                // Lazily create the event:
                if (e == null) {
                    e = new TreeModelEventImpl(this, path, ev);
                }
                try {
                    ((TreeModelListener)listeners[i+1]).treeNodesInserted(e);
                } catch (IndexOutOfBoundsException ex) {
                    LOG.log(Level.WARNING, "Visualizer: {0}", node);
                    Node n = Visualizer.findNode(node);
                    LOG.log(Level.WARNING, "Node: {0}", n);
                    if (n != null) {
                        LOG.log(Level.WARNING, "  # children: {0}", n.getChildren().getNodesCount());
                        LOG.log(Level.WARNING, "  children: {0}", n.getChildren().getClass());
                    }
                    LOG.log(Level.WARNING, "Path: {0}", Arrays.toString(path));
                    LOG.log(Level.WARNING, "ev.getArray: {0}", Arrays.toString(ev.getArray()));
                    LOG.log(Level.WARNING, "ev.getSnapshot: {0}", ev.getSnapshot());
                    throw ex;
                }
            }
        }
    }

    /** The listener */
    private static final class Listener implements NodeModel {
        /** weak reference to the model */
        private Reference<NodeTreeModel> model;

        /** Constructor.
        */
        public Listener(NodeTreeModel m) {
            model = new WeakReference<NodeTreeModel>(m);
        }

        /** Getter for the model or null.
        */
        private NodeTreeModel get(VisualizerEvent ev) {
            NodeTreeModel m = model.get();

            if ((m == null) && (ev != null)) {
                ev.getVisualizer().removeNodeModel(this);

                return null;
            }

            return m;
        }

        /** Notification of children addded event. Modifies the list of nodes
        * and fires info to all listeners.
        */
        @Override
        public void added(VisualizerEvent.Added ev) {
            NodeTreeModel m = get(ev);

            if (m == null) {
                return;
            }

            m.nodesWereInsertedInternal(ev);
        }

        /** Notification that children has been removed. Modifies the list of nodes
        * and fires info to all listeners.
        */
        @Override
        public void removed(VisualizerEvent.Removed ev) {
            NodeTreeModel m = get(ev);

            if (m == null) {
                return;
            }
            
            for (TreeView tw : m.views) {
                tw.removedNodes(ev.removed);
            }
            m.nodesWereRemoved(ev.getVisualizer(), ev.getArray(), ev.removed.toArray());
        }

        /** Notification that children has been reordered. Modifies the list of nodes
        * and fires info to all listeners.
        */
        @Override
        public void reordered(VisualizerEvent.Reordered ev) {
            NodeTreeModel m = get(ev);

            if (m == null) {
                return;
            }

            m.nodeStructureChanged(ev.getVisualizer());
        }

        /** Update a visualizer (change of name, icon, description, etc.)
        */
        @Override
        public void update(VisualizerNode v) {
            NodeTreeModel m = get(null);

            if (m == null) {
                return;
            }

            m.nodeChanged(v);
        }

        /** Notification about large change in the sub tree
         */
        @Override
        public void structuralChange(VisualizerNode v) {
            NodeTreeModel m = get(null);

            if (m == null) {
                return;
            }

            m.nodeStructureChanged(v);
        }
    }

    static Object[] computeChildren(VisualizerEvent ev) {
        int[] childIndices = ev.getArray();
        Object[] arr = new Object[childIndices.length];
        List<Node> nodes = ev.getSnapshot();
        for (int i = 0; i < arr.length; i++) {
            arr[i] = Visualizer.findVisualizer(nodes.get(childIndices[i]));
        }
        return arr;
    }

    /** Improved TreeModelEvent that does not precreate children nodes
     */
    private static class TreeModelEventImpl extends TreeModelEvent {
        private final VisualizerEvent ev;

        public TreeModelEventImpl(Object source, Object[] path, VisualizerEvent ev) {
            super(source, path, ev.getArray(), null);
            this.ev = ev;
        }

        @Override
        public Object[] getChildren() {
            if (this.children == null) {
                this.children = computeChildren(ev);
            }
            return this.children;
        }
    }
}
