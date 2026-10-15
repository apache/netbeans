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

import java.awt.EventQueue;
import org.openide.nodes.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/** List of Visualizers. This is holded by parent visualizer by a
* weak reference,
*
* @author Jaroslav Tulach
*/
final class VisualizerChildren extends Object {
    /** empty visualizer children for any leaf */
    public static final VisualizerChildren EMPTY = new VisualizerChildren();   
    private static final Logger LOG = Logger.getLogger(VisualizerChildren.class.getName());
    
    /** parent visualizer */
    public final VisualizerNode parent;

    /** visualizer nodes (children), access only from AWT dispatch thread
     * via the getVisNodes() getter
     */
    private final List<VisualizerNode> visNodes;
    
    private List<Node> snapshot;
    
    /** Empty VisualizerChildren. */
    private VisualizerChildren () {
        visNodes = Collections.emptyList();
        snapshot = Collections.emptyList();
        parent = null;
    }    
    
    /** Creates new VisualizerChildren.
     * Can be called only from EventQueue.
     */
    public VisualizerChildren(VisualizerNode parent, List<Node> snapshot) {
        this.parent = parent;
        int size = snapshot.size();
        visNodes = new ArrayList<VisualizerNode>(size);
        for (int i = 0; i < size; i++) {
            visNodes.add(null);
        }
        this.snapshot = snapshot;
    }

    /** recomputes indexes for all nodes.
     * @param tn tree node that we are looking for
     */
    private void recomputeIndexes(VisualizerNode tn) {
        final List<VisualizerNode> vn = getVisNodes(true);
        assert vn.size() == snapshot.size() : "visnodes.size()=" + vn.size()
                + " snapshot.size()=" + snapshot.size();

        for (int i = 0; i < vn.size(); i++) {
            VisualizerNode node = vn.get(i);
            if (node != null) {
                node.indexOf = i;
            }
        }

        if (tn != null && tn.indexOf == -1) {
            // not computed => force computation
            for (int i = 0; i < vn.size(); i++) {
                VisualizerNode visNode = (VisualizerNode) getChildAt(i);
                visNode.indexOf = i;
                if (visNode == tn) {
                    return;
                }
            }
        }
    }
    
    public javax.swing.tree.TreeNode getChildAt(int pos) {
        final List<VisualizerNode> vn = getVisNodes(false);
        if (pos >= vn.size()) {
            return VisualizerNode.EMPTY;
        }
        VisualizerNode visNode = vn.get(pos);
        if (visNode == null) {
            Node node = snapshot.get(pos);
            if (node == null) {
                throw new NullPointerException("snapshot: " + snapshot + " pos: " + pos + " parent: " + parent); // NOI18N
            }
            visNode = VisualizerNode.getVisualizer(this, node);
            visNode.indexOf = pos;
            vn.set(pos, visNode);
            parent.notifyVisualizerChildrenChange(false, this);
        }
        return visNode;
    }
    
    public int getChildCount() {
        return getVisNodes(false).size();
    }

    public java.util.Enumeration<VisualizerNode> children(final boolean create) {
        return new java.util.Enumeration<VisualizerNode>() {
            private int index;

            @Override
            public boolean hasMoreElements() {
                return index < getVisNodes(false).size();
            }

            @Override
            public VisualizerNode nextElement() {
                return create ? (VisualizerNode) getChildAt(index++) : getVisNodes(false).get(index++);
            }
        };
    }

    /** Delegated to us from VisualizerNode
     * 
     */
    public int getIndex(final javax.swing.tree.TreeNode p1) {
        VisualizerNode visNode = (VisualizerNode) p1;
        if (visNode.indexOf != -1) {
            final List<VisualizerNode> vn = getVisNodes(false);
            if (visNode.indexOf >= vn.size() || vn.get(visNode.indexOf) != visNode) {
                return -1;
            }
        } else {
            recomputeIndexes(visNode);
        }
        return visNode.indexOf;
    }

    final String dumpIndexes(VisualizerNode visNode) {
        StringBuilder sb = new StringBuilder();
        sb.append("EMPTY: ").append(visNode == VisualizerNode.EMPTY).
            append(", Lazy: ").
            append(snapshot.getClass().getName().endsWith("LazySnapshot")); // NOI18N
        sb.append("\nSeeking for: ").append(visNode.toId()); // NOI18N
        sb.append("\nwith parent: ").append(((VisualizerNode)visNode.getParent()) != null // NOI18N
                ? ((VisualizerNode)visNode.getParent()).toId() : "null"); // NOI18N
        sb.append("\nSeeking in : ").append(parent != null ? parent.toId() : "null").append("\n"); // NOI18N
        addVisNodesInfo(sb);
        return sb.toString();
    }
    
    private void addVisNodesInfo(StringBuilder sb) {
        final List<VisualizerNode> vn = getVisNodes(false);
        for (int i = 0; i < vn.size(); i++) {
            VisualizerNode node = vn.get(i);
            sb.append("  ").append(i); // NOI18N
            if (node != null) {
                sb.append(" = ").append(node.toId()); // NOI18N
            } else {
                sb.append(" = null"); // NOI18N
            }
            sb.append('\n'); // NOI18N
        }        
    }
    
    final String dumpEventInfo(VisualizerEvent ev) {
        StringBuilder sb = new StringBuilder();
        sb.append("\nEvent: ").append(ev.getClass().getName()); // NOI18N
        sb.append("\nOriginal event: ").append(ev.originalEvent.getClass().getName()); // NOI18N
        sb.append("\ncurrent vis. nodes:"); // NOI18N
        addVisNodesInfo(sb);
        sb.append("\nIndexes: "); // NOI18N
        int[] arr = ev.getArray();
        for (int i = 0; i < arr.length; i++) {
            sb.append(Integer.toString(arr[i]));
            sb.append(" "); // NOI18N
        }
        sb.append("\n"); // NOI18N
        sb.append(ev.originalEvent.toString());
        return sb.toString();
    }
    
    /** Notification of children addded event. Modifies the list of nodes
     * and fires info to all listeners.
     */
    public void added(VisualizerEvent.Added ev) {
        if (this != parent.getChildren()) {
            // children were replaced, quit processing event
            return;
        }        
        snapshot = ev.getSnapshot();
        ListIterator<VisualizerNode> it = getVisNodes(true).listIterator();

        int[] indxs = ev.getArray();
        int current = 0;
        int inIndxs = 0;

        while (inIndxs < indxs.length) {
            while (current++ < indxs[inIndxs]) {
                it.next();
            }
            it.add(null);
            inIndxs++;
        }

        recomputeIndexes(null);

        VisualizerNode p = this.parent;
        while (p != null) {
            Object[] listeners = p.getListenerList();
            for (int i = listeners.length - 1; i >= 0; i -= 2) {
                ((NodeModel) listeners[i]).added(ev);
            }
            p = (VisualizerNode) p.getParent();
        }
    }

    /** Notification that children has been removed. Modifies the list of nodes
     * and fires info to all listeners.
     */
   public void removed(VisualizerEvent.Removed ev) {
        if (this != parent.getChildren()) {
            // children were replaced, quit processing event
            return;
        }
        snapshot = ev.getSnapshot();
        int[] idxs = ev.getArray();
        if (idxs.length == 0) {
            return;
        }
        final List<VisualizerNode> vn = getVisNodes(true);
        if (vn.isEmpty()) {
            return;
        }
        
        assert vn.size() > idxs[idxs.length - 1] : dumpEventInfo(ev);

        int prev = Integer.MAX_VALUE;
        for (int i = idxs.length - 1; i >= 0; i--) {
            if (vn.isEmpty()) {
                continue;
            }
            assert idxs[i] < prev : "Indexes have to be descendant. Prev: " + prev + " next: " + idxs[i] + " at " + i;
            VisualizerNode visNode = vn.remove(idxs[i]);
            ev.removed.add(visNode != null ? visNode : VisualizerNode.EMPTY);
        }

        recomputeIndexes(null);

        VisualizerNode p = this.parent;
        while (p != null) {
            Object[] listeners = p.getListenerList();
            for (int i = listeners.length - 1; i >= 0; i -= 2) {
                ((NodeModel) listeners[i]).removed(ev);
            }
            p = (VisualizerNode) p.getParent();
        }

        if (vn.isEmpty()) {
            // now is empty
            this.parent.notifyVisualizerChildrenChange(true, this);
        }
    }

    /** Notification that children has been reordered. Modifies the list of nodes
     * and fires info to all listeners.
     */
    public void reordered(VisualizerEvent.Reordered ev) {
        if (this != parent.getChildren()) {
            // children were replaced, quit processing event
            return;
        }
        snapshot = ev.getSnapshot();

        int[] indxs = ev.getArray();
        final List<VisualizerNode> vn = getVisNodes(true);
        VisualizerNode[] old = vn.toArray(new VisualizerNode[0]);
        VisualizerNode[] arr = new VisualizerNode[old.length];
        int s = indxs.length;
        try {
            for (int i = 0; i < s; i++) {
                // arr[indxs[i]] = old[i];
                VisualizerNode old_i = old[i];
                int indxs_i = indxs[i];

                if (arr[indxs_i] != null) {
                    // this is bad <-- we are rewriting some old value --> there will remain some null somewhere
                    LOG.log(Level.WARNING, "Writing to this index for the second time: {0}", indxs_i); // NOI18N
                    LOG.log(Level.WARNING, "Length of indxs array: {0}", indxs.length); // NOI18N
                    LOG.log(Level.WARNING, "Length of actual array: {0}", old.length); // NOI18N
                    LOG.warning("Indices of reorder event:"); // NOI18N

                    for (int j = 0; i < indxs.length; j++) {
                        LOG.log(Level.WARNING, "\t{0}", indxs[j]); // NOI18N
                    }
                    LOG.log(Level.WARNING, "Who", new Exception());

                    return;
                }

                arr[indxs_i] = old_i;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            LOG.log(Level.WARNING, "Length of actual array: " + old.length, e); // NOI18N
            LOG.warning("Indices of reorder event:"); // NOI18N

            for (int i = 0; i < indxs.length; i++) {
                LOG.log(Level.WARNING, "\t{0}", indxs[i]); // NOI18N
            }
            return;
        }
        vn.clear();
        vn.addAll(Arrays.asList(arr));
        recomputeIndexes(null);

        VisualizerNode p = this.parent;

        while (p != null) {
            Object[] listeners = p.getListenerList();
            for (int i = listeners.length - 1; i >= 0; i -= 2) {
                ((NodeModel) listeners[i]).reordered(ev);
            }
            p = (VisualizerNode) p.getParent();
        }
    }

    @Override
    public String toString() {
        String str = "";
        if (parent != null) {
            str = "Parent: " + parent + " ";
        }
        str += "[";
        for (VisualizerNode vn : getVisNodes(false)) {
            str += vn;
            if (vn != null) {
                VisualizerChildren vch = vn.getChildren(false);
                if (vch != VisualizerChildren.EMPTY) {
                    str += vch;
                }
            }
            str += " ";
        }
        str += " {" + snapshot + "}";
        str += "]";
        return str;
    }

    final List<VisualizerNode> getVisNodes(boolean guardAccess) {
        if (guardAccess) {
            assert EventQueue.isDispatchThread();
        }
        return visNodes;
    }
}
