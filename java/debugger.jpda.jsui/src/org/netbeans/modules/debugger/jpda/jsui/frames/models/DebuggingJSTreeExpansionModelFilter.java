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
package org.netbeans.modules.debugger.jpda.jsui.frames.models;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.js.JSUtils;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.DebuggingView.DVSupport;
import org.netbeans.spi.debugger.ui.DebuggingView.DVThread;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TreeExpansionModel;
import org.netbeans.spi.viewmodel.TreeExpansionModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;
import org.openide.util.WeakSet;

/**
 * Assure that the thread we stop in is automatically expanded.
 * 
 * @author Martin Entlicher
 */
@DebuggerServiceRegistration(path="netbeans-JPDASession/JS/DebuggingView",
                             types={ TreeExpansionModelFilter.class })
public class DebuggingJSTreeExpansionModelFilter implements TreeExpansionModelFilter,
                                                            PropertyChangeListener {

    private static final Reference<JPDAThread> NO_THREAD = new WeakReference<>(null);
    private final JPDADebugger debugger;
    private final DVSupport dvSupport;
    private volatile Reference<JPDAThread> suspendedNashornThread = NO_THREAD;
    private final Set<Object> collapsedExplicitly = new WeakSet<>();
    private final Set<ModelListener> listeners = Collections.synchronizedSet(new HashSet<ModelListener>());

    public DebuggingJSTreeExpansionModelFilter(ContextProvider context) {
        debugger = context.lookupFirst(null, JPDADebugger.class);
        debugger.addPropertyChangeListener(
                JPDADebugger.PROP_CURRENT_CALL_STACK_FRAME,
                WeakListeners.propertyChange(this, JPDADebugger.PROP_CURRENT_CALL_STACK_FRAME, debugger));
        dvSupport = context.lookupFirst(null, DVSupport.class);
        currentStackFrameChanged(debugger.getCurrentCallStackFrame());
    }

    @Override
    public boolean isExpanded(TreeExpansionModel original, Object node) throws UnknownTypeException {
        if (node instanceof DVThread) {
            synchronized (this) {
                if (collapsedExplicitly.contains(node)) {
                    return false;
                }
            }
            try {
                JPDAThread thread = (JPDAThread) node.getClass().getMethod("getKey").invoke(node);
                if (thread == suspendedNashornThread.get()) {
                    return true;
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return original.isExpanded(node);
    }

    @Override
    public void nodeExpanded(Object node) {
        if (node instanceof DVThread) {
            synchronized (this) {
                collapsedExplicitly.remove(node);
            }
        }
    }

    @Override
    public void nodeCollapsed(Object node) {
        if (node instanceof DVThread) {
            synchronized (this) {
                collapsedExplicitly.add(node);
            }
        }
    }

    private void fireNodeExpanded(Object node) {
        ModelListener[] ls = listeners.toArray(new ModelListener[] {});
        if (ls.length > 0) {
            ModelEvent event = new ModelEvent.NodeChanged(this, node,
                    ModelEvent.NodeChanged.EXPANSION_MASK);
            for (ModelListener ml : ls) {
                ml.modelChanged(event);
            }
        }
    }

    @Override
    public void addModelListener(ModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeModelListener(ModelListener l) {
        listeners.remove(l);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // JPDADebugger PROP_CURRENT_CALL_STACK_FRAME
        CallStackFrame csf = (CallStackFrame) evt.getNewValue();
        currentStackFrameChanged(csf);
    }

    private void currentStackFrameChanged(CallStackFrame csf) {
        if (csf != null && (csf.getClassName().startsWith(JSUtils.NASHORN_SCRIPT_JDK) || csf.getClassName().startsWith(JSUtils.NASHORN_SCRIPT_EXT))) {
            JPDAThread thread = csf.getThread();
            suspendedNashornThread = new WeakReference<>(thread);
            try {
                Object node = dvSupport.getClass().getMethod("get", JPDAThread.class).invoke(dvSupport, thread);
                boolean explicitCollaps;
                synchronized (this) {
                    explicitCollaps = collapsedExplicitly.contains(node);
                }
                if (!explicitCollaps) {
                    fireNodeExpanded(node);
                }
            } catch (IllegalAccessException | IllegalArgumentException |
                     InvocationTargetException | NoSuchMethodException |
                     SecurityException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            suspendedNashornThread = NO_THREAD;
        }
    }

}
