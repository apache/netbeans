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
package org.netbeans.modules.cpplite.debugger.debuggingview;

import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.netbeans.modules.cpplite.debugger.CPPFrame;
import org.netbeans.modules.cpplite.debugger.CPPLiteDebugger;
import org.netbeans.modules.cpplite.debugger.CPPThread;
import org.netbeans.modules.cpplite.debugger.ThreadsCollector;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.DebuggingView.DVThread;
import org.netbeans.spi.viewmodel.CachedChildrenTreeModel;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeExpansionModel;
import org.netbeans.spi.viewmodel.TreeExpansionModelFilter;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.datatransfer.PasteType;

@DebuggerServiceRegistration(path="CPPLiteSession/DebuggingView",
                             types={TreeModel.class, ExtendedNodeModel.class, TableModel.class, TreeExpansionModelFilter.class})
public class DebuggingModel extends CachedChildrenTreeModel implements ExtendedNodeModel, TableModel, TreeExpansionModelFilter, CPPLiteDebugger.StateListener, ThreadsCollector.StateListener {

    private static final String RUNNING_THREAD_ICON =
        "org/netbeans/modules/debugger/resources/threadsView/thread_running_16.png"; // NOI18N
    private static final String SUSPENDED_THREAD_ICON =
        "org/netbeans/modules/debugger/resources/threadsView/thread_suspended_16.png"; // NOI18N
    private static final String CALL_STACK_ICON =
        "org/netbeans/modules/debugger/resources/callStackView/NonCurrentFrame.gif"; // NOI18N
    private static final String CURRENT_CALL_STACK_ICON =
        "org/netbeans/modules/debugger/resources/callStackView/CurrentFrame.gif"; // NOI18N

    private final CPPLiteDebugger debugger;
    private final List<ModelListener> listeners = new CopyOnWriteArrayList<>();
    private final Map<CPPThread, ThreadStateListener> threadStateListeners = new WeakHashMap<>();
    private final Reference<CPPThread> lastCurrentThreadRef = new WeakReference<>(null);
    private final Reference<CPPFrame> lastCurrentFrameRef = new WeakReference<>(null);
    private final Set<Object> expandedExplicitly = Collections.newSetFromMap(new WeakHashMap<>());
    private final Set<Object> collapsedExplicitly = Collections.newSetFromMap(new WeakHashMap<>());
    private final RequestProcessor RP = new RequestProcessor("Debugging Tree View Refresh", 1); // NOI18N

    public DebuggingModel(ContextProvider contextProvider) {
        debugger = contextProvider.lookupFirst(null, CPPLiteDebugger.class);
        debugger.addStateListener(WeakListeners.create(CPPLiteDebugger.StateListener.class, this, debugger));
        debugger.getThreads().addStateListener(WeakListeners.create(ThreadsCollector.StateListener.class, this, debugger.getThreads()));
    }

    @Override
    public Object getRoot() {
        return TreeModel.ROOT;
    }

    @Override
    public boolean isLeaf(Object node) throws UnknownTypeException {
        if (node instanceof CPPFrame) {
            return true;
        }
        if (node instanceof CPPThread) {
            CPPThread thread = (CPPThread) node;
            return !thread.isSuspended();
        }
        return false;
    }

    @Override
    protected Object[] computeChildren(Object parent) throws UnknownTypeException {
        if (parent == ROOT) {
            CPPThread[] threads = debugger.getThreads().getAllArray();
            for (CPPThread t : threads) {
                watchState(t);
            }
            return threads;
        }
        if (parent instanceof CPPThread) {
            CPPFrame[] stack = ((CPPThread) parent).getStack();
            if (stack != null) {
                return stack;
            } else {
                return new Object[]{};
            }
        }
        throw new UnknownTypeException(parent);
    }

    @Override
    public int getChildrenCount(Object node) throws UnknownTypeException {
        return Integer.MAX_VALUE;
    }

    @Override
    public String getDisplayName(Object node) throws UnknownTypeException {
        if (node instanceof CPPThread) {
            return ((CPPThread) node).getName();
        } else if (node instanceof CPPFrame) {
            CPPFrame frame = (CPPFrame) node;
            return frame.getName();
        }
        throw new UnknownTypeException (node);
    }

    @Override
    public String getShortDescription(Object node) throws UnknownTypeException {
        if (node instanceof CPPThread) {
            String details = ((CPPThread) node).getDetails();
            if (details == null) {
                details = ((CPPThread) node).getName();
            }
            return details;
        } else if (node instanceof CPPFrame) {
            CPPFrame frame = (CPPFrame) node;
            return frame.getDescription();
        }
        throw new UnknownTypeException (node);
    }

    @Override
    public String getIconBase(Object node) throws UnknownTypeException {
        throw new UnknownTypeException(node);
    }

    @Override
    public String getIconBaseWithExtension(Object node) throws UnknownTypeException {
        if (node instanceof CPPFrame) {
            CPPFrame currentFrame = debugger.getCurrentFrame();
            if (node.equals(currentFrame)) {
                return CURRENT_CALL_STACK_ICON;
            } else {
                return CALL_STACK_ICON;
            }
        }
        if (node instanceof CPPThread) {
            CPPThread thread = (CPPThread) node;
            return thread.isSuspended () ? SUSPENDED_THREAD_ICON : RUNNING_THREAD_ICON;
        }
        if (node == TreeModel.ROOT) {
            return ""; // will not be displayed
        }
        throw new UnknownTypeException (node);
    }

    @Override
    public boolean canCopy(Object node) throws UnknownTypeException {
        return false;
    }

    @Override
    public boolean canCut(Object node) throws UnknownTypeException {
        return false;
    }

    @Override
    public boolean canRename(Object node) throws UnknownTypeException {
        return false;
    }

    @Override
    public Transferable clipboardCopy(Object node) throws IOException, UnknownTypeException {
        throw new UnknownTypeException(node);
    }

    @Override
    public Transferable clipboardCut(Object node) throws IOException, UnknownTypeException {
        throw new UnknownTypeException(node);
    }

    @Override
    public PasteType[] getPasteTypes(Object node, Transferable t) throws UnknownTypeException {
        throw new UnknownTypeException(node);
    }

    @Override
    public void setName(Object node, String name) throws UnknownTypeException {
        throw new UnknownTypeException(node);
    }

    @Override
    public boolean isReadOnly(Object node, String columnID) throws UnknownTypeException {
        return true;
    }

    @Override
    public Object getValueAt(Object node, String columnID) throws UnknownTypeException {
        if (columnID.equals("suspend")) {
            if (node instanceof CPPThread) {
                CPPThread thread = (CPPThread) node;
                CPPThread.Status status = thread.getStatus();
                switch (status) {
                    case CREATED:
                        return Boolean.FALSE;
                    case EXITED:
                        return null;
                    case RUNNING:
                        return Boolean.FALSE;
                    case SUSPENDED:
                        return Boolean.TRUE;
                    default:
                        throw new IllegalStateException("Unknown status: " + status);
                }
            } else {
                return null;
            }
        }
        throw new UnknownTypeException(node.toString());
    }

    @Override
    public void setValueAt(Object node, String columnID, Object value) throws UnknownTypeException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean isExpanded(TreeExpansionModel original, Object node) throws UnknownTypeException {
        synchronized (this) {
            if (expandedExplicitly.contains(node)) {
                return true;
            }
            if (collapsedExplicitly.contains(node)) {
                return false;
            }
        }
        if (node instanceof CPPThread) {
            CPPThread thread = (CPPThread) node;
            return thread.isSuspended() && debugger.getCurrentThread() == thread;
        }
        return original.isExpanded(node);
    }

    @Override
    public void nodeExpanded(Object node) {
        synchronized (this) {
            expandedExplicitly.add(node);
            collapsedExplicitly.remove(node);
        }
        if (node instanceof CPPThread) {
            fireNodeChange(node, ModelEvent.NodeChanged.DISPLAY_NAME_MASK);
        }
    }

    @Override
    public void nodeCollapsed(Object node) {
        synchronized (this) {
            expandedExplicitly.remove(node);
            collapsedExplicitly.add(node);
        }
        if (node instanceof CPPThread) {
            fireNodeChange(node, ModelEvent.NodeChanged.DISPLAY_NAME_MASK);
        }
    }

    @Override
    public void addModelListener(ModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeModelListener (ModelListener l) {
        listeners.remove(l);
    }

    private void watchState(CPPThread t) {
        synchronized (threadStateListeners) {
            if (!threadStateListeners.containsKey(t)) {
                threadStateListeners.put(t, new ThreadStateListener(t));
            }
        }
    }

    @Override
    public void currentThread(CPPThread thread) {
        if (thread != null) {
            fireNodeChange(thread, ModelEvent.NodeChanged.DISPLAY_NAME_MASK | ModelEvent.NodeChanged.ICON_MASK | ModelEvent.NodeChanged.EXPANSION_MASK);
        }
    }

    @Override
    public void currentFrame(CPPFrame frame) {
        if (frame != null) {
            fireNodeChange(frame.getThread(), ModelEvent.NodeChanged.EXPANSION_MASK);
            fireNodeChange(frame, ModelEvent.NodeChanged.DISPLAY_NAME_MASK | ModelEvent.NodeChanged.ICON_MASK);
        }
    }

    @Override
    public void suspended(boolean suspended) {
    }

    @Override
    public void finished() {
        clearCache();
    }

    private void fireModelChange(ModelEvent me) {
        for (ModelListener ls : listeners) {
            ls.modelChanged(me);
        }
    }

    private void fireNodeChange(Object node, int mask) {
        ModelEvent event = new ModelEvent.NodeChanged(this, node, mask);
        for (ModelListener ml : listeners) {
            ml.modelChanged (event);
        }
    }

    @Override
    public void threadStarted(CPPThread thread) {
        refreshCache(ROOT);
        ModelEvent ev = new ModelEvent.NodeChanged(this, ROOT, ModelEvent.NodeChanged.CHILDREN_MASK);
        fireModelChange(ev);
    }

    @Override
    public void threadDied(CPPThread thread) {
        refreshCache(ROOT);
        ModelEvent ev = new ModelEvent.NodeChanged(this, ROOT, ModelEvent.NodeChanged.CHILDREN_MASK);
        fireModelChange(ev);
    }

    private class ThreadStateListener implements PropertyChangeListener {

        private final Reference<CPPThread> tr;
        // currently waiting / running refresh task
        // there is at most one
        private RequestProcessor.Task task;
        private final PropertyChangeListener propertyChangeListener;

        public ThreadStateListener(CPPThread t) {
            this.tr = new WeakReference<>(t);
            this.propertyChangeListener = WeakListeners.propertyChange(this, t);
            t.addPropertyChangeListener(propertyChangeListener);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!evt.getPropertyName().equals(DVThread.PROP_SUSPENDED)) return ;
            CPPThread t = tr.get();
            if (t == null) return ;
            // Refresh the children of the thread (stack frames) when the thread
            // gets suspended or is resumed
            synchronized (this) {
                if (task == null) {
                    task = RP.create(new Refresher());
                }
                int delay = 100;
                task.schedule(delay);
            }
        }

        PropertyChangeListener getThreadPropertyChangeListener() {
            return propertyChangeListener;
        }

        private class Refresher extends Object implements Runnable {
            @Override
            public void run() {
                CPPThread thread = tr.get();
                if (thread != null) {
                    try {
                        recomputeChildren(thread);
                    } catch (UnknownTypeException ex) {
                        refreshCache(thread);
                    }
                    ModelEvent event = new ModelEvent.NodeChanged(this, thread);
                    fireModelChange(event);
                }
            }
        }
    }


}
