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
package org.openide.explorer.view;

import org.openide.nodes.Node;
import org.openide.util.*;


import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import java.util.*;

import javax.swing.*;
import org.openide.nodes.Children;


/** Model for displaying the nodes in list and choice.
*
* @author Jaroslav Tulach
*/
public class NodeListModel extends AbstractListModel implements ComboBoxModel {
    static final long serialVersionUID = -1926931095895356820L;

    /** listener used to listen to changes in trees */
    private transient Listener listener;

    /** parent node */
    private transient VisualizerNode parent;

    /** should parent node be visible? */
    private transient boolean showParent;

    /** originally selected item */
    private transient Object selectedObject;

    /** previous size */
    private transient int size;

    /** depth to display */
    private int depth = 1;

    /** map that assignes to each visualizer number of its children till
    * the specified depth.
    */
    private Map<VisualizerNode, Info> childrenCount;

    /** Creates new model.
    */
    public NodeListModel() {
        parent = VisualizerNode.EMPTY;
        selectedObject = VisualizerNode.EMPTY;
        clearChildrenCount();
    }

    /** Creates new model.
    * @param root the root of the model
    */
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public NodeListModel(Node root) {
        this();
        setNode(root);
    }

    /** Changes the root of the model. This is thread safe method.
    * @param root the root of the model
    */
    public void setNode(Node root) {
        setNode(root, false);
    }

    final void setNode(final Node root, final boolean showRoot) {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                if (!Children.MUTEX.isReadAccess() && !Children.MUTEX.isWriteAccess()) {
                    Children.MUTEX.readAccess(this);
                    return;
                }
                VisualizerNode v = VisualizerNode.getVisualizer(null, root);

                if (v == parent && showParent == showRoot) {
                    // no change
                    return;
                }

                removeAll();
                parent.removeNodeModel(listener());

                showParent = showRoot;
                parent = v;
                selectedObject = v;
                clearChildrenCount();

                addAll();
                parent.addNodeModel(listener());
            }
        });
    }

    /** Depth of nodes to display.
    * @param depth the depth
    */
    public void setDepth(int depth) {
        if (depth != this.depth) {
            this.depth = depth;
            clearChildrenCount();

            Mutex.EVENT.readAccess(
                new Runnable() {
                @Override
                    public void run() {
                        removeAll();
                        addAll();
                    }
                }
            );
        }
    }

    /** Getter for depth.
    * @return number of levels to display
    */
    public int getDepth() {
        return depth;
    }

    /** Getter for the listener. Only from AWT-QUEUE.
    */
    private Listener listener() {
        if (listener == null) {
            listener = new Listener(this);
        }
        return listener;
    }

    //
    // model methods
    //

    /** Number of elements in the model.
    */
    @Override
    public int getSize() {
        int s = findSize(parent, showParent, -1, depth);
        return s;
    }

    /** Child at given index.
    */
    @Override
    public Object getElementAt(int i) {
        return findElementAt(parent, showParent, i, -1, depth);
    }

    /** Finds index of given object.
    * @param o object produced by this model
    * @return index, or -1 if the object is not in the list
    */
    public int getIndex(Object o) {
        getSize();
        @SuppressWarnings("element-type-mismatch")
        Info i = childrenCount.get(o);
        return (i == null) ? (-1) : i.index;
    }

    /** Currently selected item.
    */
    @Override
    public void setSelectedItem(Object anObject) {
        if (selectedObject != anObject) {
            selectedObject = anObject;
            fireContentsChanged(this, -1, -1);
        }
    }

    @Override
    public Object getSelectedItem() {
        return selectedObject;
    }

    //
    // modification of the counting model
    //
    private void clearChildrenCount() {
        childrenCount = new HashMap<VisualizerNode, Info>(17);
    }

    /** Finds size of sub children excluding vis node.
    *
    * @param vis the visualizer to find the size for
    * @param index the index that should be assigned to vis
    * @param depth the depth to scan
    * @return number of children
    */
    private int findSize(VisualizerNode vis, boolean includeOwnself, int index, int depth) {
        Info info = childrenCount.get(vis);
        if (info != null) {
            return info.childrenCount;
        }

        if (includeOwnself) {
            index++;
        }

        // only my children
        int tmp = 0;

        info = new Info();
        info.depth = depth;
        info.index = index;

        /*if (depth == 1) {
            // enough to know the number of children
            size += vis.getChildren().getChildCount();
        } else */
        if (depth-- > 0) {
            Enumeration it = vis.children();
            while (it.hasMoreElements()) {
                VisualizerNode v = (VisualizerNode) it.nextElement();
                // count node v
                tmp++;
                // now count children of node v
                tmp += findSize(v, false, index + tmp, depth);
            }
        }

        info.childrenCount = includeOwnself ? tmp + 1 : tmp;
        childrenCount.put(vis, info);
        return tmp;
    }

    /** Finds the child with requested index.
    *
    * @param vis the visualizer to find the size for
    * @param indx the index of requested child
    * @param depth the depth to scan
    * @return the children
    */
    private VisualizerNode findElementAt(VisualizerNode vis, boolean countSelf, int indx, int realIndx, int depth) {
        if (countSelf) {
            if (indx == 0) {
                return vis;
            } else {
                indx--;
            }
        }

        if (--depth == 0) {
            // last depth is handled in special way
            return (VisualizerNode) vis.getChildAt(indx);
        }

        Enumeration it = vis.children();
        while (it.hasMoreElements()) {
            VisualizerNode v = (VisualizerNode) it.nextElement();

            if (indx-- == 0) {
                return v;
            }

            int s = findSize(v, false, ++realIndx, depth);

            if (indx < s) {
                // search this child
                return findElementAt(v, false, indx, realIndx, depth);
            }

            // go to next child
            indx -= s;
            realIndx += s;
        }

        return vis;
    }

    /** Finds a depth for given model & object. Used from renderer.
    * @param m model
    * @param o the visualizer node
    * @return depth or 0 if not found
    */
    static int findVisualizerDepth(ListModel m, VisualizerNode o) {
        if (m instanceof NodeListModel) {
            NodeListModel n = (NodeListModel) m;
            Info i = n.childrenCount.get(o);

            if (i != null) {
                return n.depth - i.depth - 1;
            }
        }

        return 0;
    }

    //
    // Modifications
    //
    final void addAll() {
        size = getSize();

        if (size > 0) {
            fireIntervalAdded(this, 0, size - 1);
        }
    }

    final void removeAll() {
        if (size > 0) {
            fireIntervalRemoved(this, 0, size - 1);
        }
    }

    final void changeAll() {
        size = getSize();
        clearChildrenCount();
        if (size > 0) {
            fireContentsChanged(this, 0, size);
        }
    }

    final void added(VisualizerEvent.Added ev) {
        VisualizerNode v = ev.getVisualizer();
        int[] indices = ev.getArray();

        //fire that model has been changed only when event source's (visualizer)
        //children are shown in the list
        if ((cachedDepth(v) <= 0) || (indices.length == 0)) {
            return;
        }

        clearChildrenCount();
        size = getSize();

        int seg = (parent == v) ? 0 : getIndex(v);
        fireIntervalAdded(this, indices[0] + seg, indices[indices.length - 1] + seg);
    }

    final void removed(VisualizerEvent.Removed ev) {
        VisualizerNode v = ev.getVisualizer();
        int[] indices = ev.getArray();

        //fire that model has been changed only when event source's (visualizer)
        //children are shown in the list
        if ((cachedDepth(v) <= 0) || (indices.length == 0)) {
            return;
        }

        clearChildrenCount();

        int seg = (parent == v) ? 0 : getIndex(v);
        fireIntervalRemoved(this, indices[0] + seg, indices[indices.length - 1] + seg);
    }

    final void update(VisualizerNode v) {
        // ensure the model is computed
        getSize();

        Info i = childrenCount.get(v);

        if (i != null) {
            fireContentsChanged(this, i.index, i.index);
        }
    }

    private int cachedDepth(VisualizerNode v) {
        getSize();

        Info i = childrenCount.get(v);

        if (i != null) {
            return i.depth;
        }

        // v is not in the model
        return -1;
    }

    /** The listener */
    private static final class Listener implements NodeModel {
        /** weak reference to the model */
        private Reference<NodeListModel> model;

        /** Constructor.
        */
        public Listener(NodeListModel m) {
            model = new WeakReference<NodeListModel>(m);
        }

        /** Getter for the model or null.
        */
        private NodeListModel get(VisualizerEvent ev) {
            NodeListModel m = model.get();

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
            NodeListModel m = get(ev);

            if (m == null) {
                return;
            }

            m.added(ev);
        }

        /** Notification that children has been removed. Modifies the list of nodes
        * and fires info to all listeners.
        */
        @Override
        public void removed(VisualizerEvent.Removed ev) {
            NodeListModel m = get(ev);

            if (m == null) {
                return;
            }

            m.removed(ev);
        }

        /** Notification that children has been reordered. Modifies the list of nodes
        * and fires info to all listeners.
        */
        @Override
        public void reordered(VisualizerEvent.Reordered ev) {
            NodeListModel m = get(ev);

            if (m == null) {
                return;
            }

            m.changeAll();
        }

        /** Update a visualizer (change of name, icon, description, etc.)
        */
        @Override
        public void update(VisualizerNode v) {
            NodeListModel m = get(null);

            if (m == null) {
                return;
            }

            m.update(v);
        }

        /** Notification about big change in children
        */
        @Override
        public void structuralChange(VisualizerNode v) {
            NodeListModel m = get(null);

            if (m == null) {
                return;
            }

            m.changeAll();
        }
    }

    /** Info for a component in model
    */
    private static final class Info extends Object {
        public int childrenCount;
        public int depth;
        public int index;

        Info() {
        }

        @Override
        public String toString() {
            return "Info[childrenCount=" + childrenCount + ", depth=" + depth + // NOI18N
            ", index=" + index; // NOI18N
        }
    }
}
