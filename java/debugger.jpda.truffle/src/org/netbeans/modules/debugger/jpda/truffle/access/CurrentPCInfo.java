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

package org.netbeans.modules.debugger.jpda.truffle.access;

import com.sun.jdi.AbsentInformationException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.function.IntFunction;

import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.LocalVariable;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.truffle.ast.TruffleNode;
import org.netbeans.modules.debugger.jpda.truffle.frames.TruffleStackFrame;
import org.netbeans.modules.debugger.jpda.truffle.frames.TruffleStackInfo;
import org.netbeans.modules.debugger.jpda.truffle.source.SourcePosition;
import org.netbeans.modules.debugger.jpda.truffle.vars.impl.TruffleScope;

/**
 * Container of information about the current program counter.
 */
public final class CurrentPCInfo {
    
    public static final String PROP_SELECTED_FRAME = "selectedFrame";           // NOI18N
    
    private final LocalVariable stepCmd;
    private final Reference<JPDAThread> threadRef;
    private final SourcePosition sp;
    private final TruffleScope[] scopes;
    private final TruffleStackFrame topFrame;
    private final TruffleStackInfo stack;
    private final IntFunction<TruffleNode> truffleNodes;
    private volatile TruffleStackFrame selectedStackFrame; // the top frame initially
    
    private PropertyChangeSupport pchs = new PropertyChangeSupport(this);
    
    CurrentPCInfo(LocalVariable stepCmd, JPDAThread thread, SourcePosition sp,
                  TruffleScope[] scopes, TruffleStackFrame topFrame,
                  TruffleStackInfo stack, IntFunction<TruffleNode> truffleNodes) {
        this.stepCmd = stepCmd;
        this.threadRef = new WeakReference<>(thread);
        this.sp = sp;
        this.scopes = scopes;
        this.topFrame = topFrame;
        this.stack = stack;
        this.truffleNodes = truffleNodes;
        selectedStackFrame = topFrame;
    }
    
    public LocalVariable getStepCommandVar() {
        return stepCmd;
    }
    
    public JPDAThread getThread() {
        return threadRef.get();
    }
    
    public SourcePosition getSourcePosition() {
        return sp;
    }

    public TruffleScope[] getScopes() {
        return scopes;
    }
    
    public TruffleStackFrame getTopFrame() {
        return topFrame;
    }

    public TruffleStackInfo getStack() {
        return stack;
    }

    public TruffleStackFrame getSelectedStackFrame() {
        return selectedStackFrame;
    }

    public void setSelectedStackFrame(TruffleStackFrame selectedStackFrame) {
        if (selectedStackFrame != null) {
            ((JPDADebuggerImpl) selectedStackFrame.getDebugger()).setCurrentCallStackFrame(null);
//            try {
//                selectedStackFrame.getThread().getCallStack(0, 1)[0].makeCurrent();
//            } catch (AbsentInformationException ex) {}
            selectedStackFrame.getDebugger().getSession().setCurrentLanguage(TruffleStrataProvider.TRUFFLE_STRATUM);
        }
        TruffleStackFrame old = this.selectedStackFrame;
        this.selectedStackFrame = selectedStackFrame;
        if (old != selectedStackFrame) {
            pchs.firePropertyChange(PROP_SELECTED_FRAME, old, selectedStackFrame);
        }
    }
    
    public TruffleNode getAST(TruffleStackFrame frame) {
        if (frame == null) {
            return null;
        }
        return truffleNodes.apply(frame.getDepth());
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pchs.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pchs.removePropertyChangeListener(listener);
    }

}
