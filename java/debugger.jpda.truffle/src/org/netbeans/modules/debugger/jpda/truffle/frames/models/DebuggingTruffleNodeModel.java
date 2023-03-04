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

package org.netbeans.modules.debugger.jpda.truffle.frames.models;

import com.sun.jdi.AbsentInformationException;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.LocalVariable;
import org.netbeans.api.debugger.jpda.MonitorInfo;
import org.netbeans.api.debugger.jpda.This;
import org.netbeans.modules.debugger.jpda.truffle.Utils;
import org.netbeans.modules.debugger.jpda.truffle.access.CurrentPCInfo;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import org.netbeans.modules.debugger.jpda.truffle.frames.TruffleStackFrame;
import org.netbeans.modules.debugger.jpda.truffle.vars.impl.TruffleScope;
import org.netbeans.modules.debugger.jpda.ui.debugging.JPDADVThread;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.ExtendedNodeModelFilter;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;

import org.openide.util.WeakListeners;
import org.openide.util.WeakSet;
import org.openide.util.datatransfer.PasteType;

@DebuggerServiceRegistration(path="netbeans-JPDASession/DebuggingView",
                             types=ExtendedNodeModelFilter.class,
                             position=23000)
public class DebuggingTruffleNodeModel implements ExtendedNodeModelFilter {
    
    private final JPDADebugger debugger;
    private final List<ModelListener> listeners = new CopyOnWriteArrayList<ModelListener>();
    private final WeakSet<CurrentPCInfo> cpisListening = new WeakSet<CurrentPCInfo>();
    private final CurrentInfoPropertyChangeListener cpiChL = new CurrentInfoPropertyChangeListener();
    
    public DebuggingTruffleNodeModel(ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
        debugger.addPropertyChangeListener(JPDADebugger.PROP_CURRENT_THREAD, WeakListeners.propertyChange(cpiChL, debugger));
    }

    @Override
    public boolean canRename(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        return original.canRename(node);
    }

    @Override
    public boolean canCopy(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        return original.canCopy(node);
    }

    @Override
    public boolean canCut(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        return original.canCut(node);
    }

    @Override
    public Transferable clipboardCopy(ExtendedNodeModel original, Object node) throws IOException, UnknownTypeException {
        return original.clipboardCopy(node);
    }

    @Override
    public Transferable clipboardCut(ExtendedNodeModel original, Object node) throws IOException, UnknownTypeException {
        return original.clipboardCut(node);
    }

    @Override
    public PasteType[] getPasteTypes(ExtendedNodeModel original, Object node, Transferable t) throws UnknownTypeException {
        return original.getPasteTypes(node, t);
    }

    @Override
    public void setName(ExtendedNodeModel original, Object node, String name) throws UnknownTypeException {
        original.setName(node, name);
    }

    @Override
    public String getIconBaseWithExtension(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        if (node instanceof TruffleStackFrame) {
            return original.getIconBaseWithExtension(EmptyCallStackFrame.INSTANCE);
        }
        if (node instanceof TruffleScope) {
            return null;
        }
        return original.getIconBaseWithExtension(node);
    }

    @Override
    @NbBundle.Messages({"# {0} - thread name",
                        "# {1} - source location",
                        "CTL_Thread_State_Truffle_Suspended_At=''{0}'' suspended at ''{1}''",})
    public String getDisplayName(NodeModel original, Object node) throws UnknownTypeException {
        if (node instanceof TruffleStackFrame) {
            TruffleStackFrame tf = (TruffleStackFrame) node;
            String displayName = tf.getDisplayName();
            JPDAThread thread = tf.getThread();
            CurrentPCInfo currentPCInfo = TruffleAccess.getCurrentPCInfo(thread);
            if (currentPCInfo != null) {
                synchronized (cpisListening) {
                    if (!cpisListening.contains(currentPCInfo)) {
                        currentPCInfo.addPropertyChangeListener(
                                WeakListeners.propertyChange(cpiChL, currentPCInfo));
                        cpisListening.add(currentPCInfo);
                    }
                }
                TruffleStackFrame selectedStackFrame = null;
                if (debugger.getCurrentThread() == thread) {
                    selectedStackFrame = currentPCInfo.getSelectedStackFrame();
                }
                if (selectedStackFrame == tf) {
                    displayName = Utils.toHTML(displayName, true, tf.isInternal(), null);
                } else if (tf.isInternal()) {
                    displayName = Utils.toHTML(displayName, false, true, null);
                }
            }
            return displayName;
        } else if (node instanceof JPDADVThread) {
            JPDAThread thread = ((JPDADVThread) node).getKey();
            if (thread.isSuspended()) {
                CurrentPCInfo currentPCInfo = TruffleAccess.getCurrentPCInfo(thread);
                if (currentPCInfo != null) {
                    String where = currentPCInfo.getTopFrame().getSourceLocation();
                    return Bundle.CTL_Thread_State_Truffle_Suspended_At(thread.getName(), where);
                }
            }
        }
        return original.getDisplayName(node);
    }

    @Override
    public String getIconBase(NodeModel original, Object node) throws UnknownTypeException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String getShortDescription(NodeModel original, Object node) throws UnknownTypeException {
        if (node instanceof TruffleStackFrame) {
            return ((TruffleStackFrame) node).getDisplayName();
        } else {
            return original.getShortDescription(node);
        }
    }
    
    private void fireDisplayNamesChanged() {
        ModelEvent evt = new ModelEvent.NodeChanged(this, null);
        for (ModelListener l : listeners) {
            l.modelChanged(evt);
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
    
    private class CurrentInfoPropertyChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            fireDisplayNamesChanged();
        }
        
    }
    
    private static final class EmptyCallStackFrame implements CallStackFrame {
        
        static final CallStackFrame INSTANCE = new EmptyCallStackFrame();

        @Override
        public int getLineNumber(String struts) {
            return 1;
        }

        @Override
        public int getFrameDepth() {
            return 0;
        }

        @Override
        public EditorContext.Operation getCurrentOperation(String struts) {
            return null;
        }

        @Override
        public String getMethodName() {
            return "";
        }

        @Override
        public String getClassName() {
            return "";
        }

        @Override
        public String getDefaultStratum() {
            return "";
        }

        @Override
        public List<String> getAvailableStrata() {
            return Collections.emptyList();
        }

        @Override
        public String getSourceName(String struts) throws AbsentInformationException {
            return "";
        }

        @Override
        public String getSourcePath(String stratum) throws AbsentInformationException {
            return "";
        }

        @Override
        public LocalVariable[] getLocalVariables() throws AbsentInformationException {
            return new LocalVariable[] {};
        }

        @Override
        public This getThisVariable() {
            return null;
        }

        @Override
        public void makeCurrent() {
        }

        @Override
        public boolean isObsolete() {
            return false;
        }

        @Override
        public void popFrame() {
        }

        @Override
        public JPDAThread getThread() {
            return null;
        }

        @Override
        public List<MonitorInfo> getOwnedMonitors() {
            return Collections.emptyList();
        }
        
    }
    
}
