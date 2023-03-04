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

package org.netbeans.modules.debugger.ui.models;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.Vector;

import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Properties;
import org.netbeans.spi.viewmodel.ReorderableTreeModel;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;


/**
 * @author   Jan Jancura
 */
public class WatchesTreeModel implements ReorderableTreeModel {
    
    private static final String PROP_SHOW_PINNED_WATCHES = "showPinnedWatches"; // NOI18N
    private static final Properties PROPERTIES = Properties.getDefault().getProperties("debugger").getProperties("watchesProps");    // NOI18N

    private Listener listener;
    private Vector listeners = new Vector ();
    private final EmptyWatch EMPTY_WATCH = new EmptyWatch();
    static final ShowPinnedWatches showPinnedWatches = new ShowPinnedWatches();
    
    /** 
     *
     * @return threads contained in this group of threads
     */
    public Object getRoot () {
        return ROOT;
    }
    
    /** 
     *
     * @return threads contained in this group of threads
     */
    public Object[] getChildren (Object parent, int from, int to) 
    throws UnknownTypeException {
        if (parent == ROOT) {
            Watch[] wsTemp = DebuggerManager.getDebuggerManager ().
                getWatches ();
            Object[] ws;
            if (showPinnedWatches.isShowPinnedWatches()) {
                ws = new Object[wsTemp.length + 1];
                System.arraycopy(wsTemp, 0, ws, 0, wsTemp.length);
            } else {
                int numwatches = 0;
                for (Watch w : wsTemp) {
                    if (w.getPin() == null) {
                        numwatches++;
                    }
                }
                ws = new Object[numwatches + 1];
                numwatches = 0;
                for (Watch w : wsTemp) {
                    if (w.getPin() == null) {
                        ws[numwatches++] = w;
                    }
                }
                //showPinnedWatches.areAnyPinnedWatches = numwatches < wsTemp.length;
            }
            ws[ws.length - 1] = EMPTY_WATCH;
            if (listener == null)
                listener = new Listener (this);
            to = Math.min(ws.length, to);
            from = Math.min(ws.length, from);
            if (from > 0 || to < ws.length) {
                Object[] fws = new Object [to - from];
                System.arraycopy (ws, from, fws, 0, to - from);
                return fws;
            } else {
                return ws;
            }
        } else
        throw new UnknownTypeException (parent);
    }
    
    /**
     * Returns number of children for given node.
     * 
     * @param   node the parent node
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve children for given node type
     *
     * @return  true if node is leaf
     */
    public int getChildrenCount (Object node) throws UnknownTypeException {
        if (node == ROOT) {
            if (listener == null)
                listener = new Listener (this);
            // Performance, see issue #59058.
            return Integer.MAX_VALUE;
            //return DebuggerManager.getDebuggerManager ().getWatches ().length;
        } else
        throw new UnknownTypeException (node);
    }
    
    public boolean isLeaf (Object node) throws UnknownTypeException {
        if (node == ROOT) return false;
        if (node instanceof Watch) return true;
        if (node instanceof EmptyWatch) return true;
        throw new UnknownTypeException (node);
    }

    public boolean canReorder(Object parent) throws UnknownTypeException {
        return parent == ROOT;
    }

    public void reorder(Object parent, int[] perm) throws UnknownTypeException {
        if (parent == ROOT) {
            int numWatches = DebuggerManager.getDebuggerManager ().getWatches ().length;
            // Resize - filters can add or remove children
            perm = resizePermutation(perm, numWatches);
            DebuggerManager.getDebuggerManager ().reorderWatches(perm);
        } else {
            throw new UnknownTypeException(parent);
        }
    }

    private static int[] resizePermutation(int[] perm, int size) {
        if (size == perm.length) return perm;
        int[] nperm = new int[size];
        if (size < perm.length) {
            int j = 0;
            for (int i = 0; i < perm.length; i++) {
                int p = perm[i];
                if (p < size) {
                    nperm[j++] = p;
                }
            }
        } else {
            System.arraycopy(perm, 0, nperm, 0, perm.length);
            for (int i = perm.length; i < size; i++) {
                nperm[i] = i;
            }
        }
        return nperm;
    }

    public void addModelListener (ModelListener l) {
        listeners.add (l);
    }

    public void removeModelListener (ModelListener l) {
        listeners.remove (l);
    }
    
    private void fireTreeChanged () {
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        for (i = 0; i < k; i++)
            ((ModelListener) v.get (i)).modelChanged (
                new ModelEvent.NodeChanged(this, ROOT, ModelEvent.NodeChanged.CHILDREN_MASK)
            );
    }
    
    void fireWatchPropertyChanged (Watch b, String propertyName) {
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        for (i = 0; i < k; i++)
            ((ModelListener) v.get (i)).modelChanged (
                //new ModelEvent.TableValueChanged (this, b, "DefaultWatchesColumn")
                new ModelEvent.NodeChanged(this, b)
            );
    }

    
    // innerclasses ............................................................
    
    private static class Listener extends DebuggerManagerAdapter implements 
    PropertyChangeListener {
        
        private WeakReference model;
        
        public Listener (
            WatchesTreeModel tm
        ) {
            model = new WeakReference (tm);
            DebuggerManager.getDebuggerManager ().addDebuggerListener (
                DebuggerManager.PROP_WATCHES,
                this
            );
            Watch[] ws = DebuggerManager.getDebuggerManager ().
                getWatches ();
            int i, k = ws.length;
            for (i = 0; i < k; i++)
                ws [i].addPropertyChangeListener (this);
            PROPERTIES.addPropertyChangeListener(this);
        }
        
        private WatchesTreeModel getModel () {
            WatchesTreeModel m = (WatchesTreeModel) model.get ();
            if (m == null) {
                DebuggerManager.getDebuggerManager ().removeDebuggerListener (
                    DebuggerManager.PROP_WATCHES,
                    this
                );
                Watch[] ws = DebuggerManager.getDebuggerManager ().
                    getWatches ();
                int i, k = ws.length;
                for (i = 0; i < k; i++)
                    ws [i].removePropertyChangeListener (this);
            }
            return m;
        }
        
        public void watchAdded (Watch watch) {
            WatchesTreeModel m = getModel ();
            if (m == null) return;
            watch.addPropertyChangeListener (this);
            m.fireTreeChanged ();
        }
        
        public void watchRemoved (Watch watch) {
            WatchesTreeModel m = getModel ();
            if (m == null) return;
            watch.removePropertyChangeListener (this);
            m.fireTreeChanged ();
        }
    
        public void propertyChange (PropertyChangeEvent evt) {
            WatchesTreeModel m = getModel ();
            if (m == null) return;
            Object source = evt.getSource();
            if (source == PROPERTIES && PROP_SHOW_PINNED_WATCHES.equals(evt.getPropertyName())) {
                m.fireTreeChanged();
                return ;
            }
            if (!(source instanceof Watch))
                return;
            Watch w = (Watch) evt.getSource ();
            m.fireWatchPropertyChanged (w, evt.getPropertyName ());
        }
    }

    static class ShowPinnedWatches {

        boolean isShowPinnedWatches() {
            return PROPERTIES.getBoolean(PROP_SHOW_PINNED_WATCHES, false);
        }

        boolean isShowPinnedWatchesEnabled() {
            return true;//areAnyPinnedWatches;
        }

        void setShowPinnedWatches(boolean showPinnedWatches) {
            PROPERTIES.setBoolean(PROP_SHOW_PINNED_WATCHES, showPinnedWatches);
        }

    }

    /**
     * An item displayed at the end of watches that can be used to enter new watch expressions.
     */
    class EmptyWatch {

        public void setExpression(String expr) {
            String infoStr = NbBundle.getBundle (WatchesTreeModel.class).getString("CTL_WatchesModel_Empty_Watch_Hint");
            infoStr = "<" + infoStr + ">";
            if (expr == null || expr.trim().length() == 0 || infoStr.equals(expr)) {
                return; // cancel action
            }
            Vector v = (Vector) listeners.clone ();
            int i, k = v.size ();
            for (i = 0; i < k; i++)
                ((ModelListener) v.get (i)).modelChanged (
                    new ModelEvent.NodeChanged (WatchesTreeModel.this, EmptyWatch.this)
                );
            
            DebuggerManager.getDebuggerManager().createWatch(expr);
        }

    }

}
