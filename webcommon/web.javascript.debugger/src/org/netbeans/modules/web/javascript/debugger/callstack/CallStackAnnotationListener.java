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
package org.netbeans.modules.web.javascript.debugger.callstack;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.javascript.debugger.MiscEditorUtil;
import org.netbeans.modules.web.javascript.debugger.annotation.CallStackAnnotation;
import org.netbeans.modules.web.javascript.debugger.annotation.CurrentLineAnnotation;
import org.netbeans.modules.web.javascript.debugger.browser.ProjectContext;
import org.netbeans.modules.web.webkit.debugging.api.Debugger;
import org.netbeans.modules.web.webkit.debugging.api.debugger.CallFrame;
import org.netbeans.modules.web.webkit.debugging.api.debugger.Script;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.text.Annotation;
import org.openide.text.Line;

@DebuggerServiceRegistration(types=LazyDebuggerManagerListener.class)
public class CallStackAnnotationListener extends DebuggerManagerAdapter
                                         implements PropertyChangeListener {
    
    private ProjectContext pc;
    private final List<Annotation> annotations = new ArrayList<Annotation>();
    private final Map<Debugger, Debugger.Listener> debuggerListeners = new HashMap<>();
    
    @Override
    public String[] getProperties() {
        return new String[] { DebuggerManager.PROP_DEBUGGER_ENGINES,
                              DebuggerManager.PROP_CURRENT_ENGINE };
    }
    
    @Override
    public void engineAdded(DebuggerEngine engine) {
        Debugger d = engine.lookupFirst("", Debugger.class);
        if (d != null) {
            addDebuggerListener(d);
            d.addPropertyChangeListener(this);
            pc = engine.lookupFirst(null, ProjectContext.class);
            List<CallFrame> stackTrace;
            if (d.isSuspended()) {
                stackTrace = d.getCurrentCallStack();
            } else {
                stackTrace = Collections.emptyList();
            }
            updateAnnotations(d, stackTrace);
        }
    }

    @Override
    public void engineRemoved(DebuggerEngine engine) {
        Debugger d = engine.lookupFirst("", Debugger.class);
        if (d != null) {
            removeDebuggerListener(d);
            d.removePropertyChangeListener(this);
            pc = null;
            updateAnnotations(null, Collections.<CallFrame>emptyList());
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if (Debugger.PROP_CURRENT_FRAME.equals(propertyName)) {
            CallFrame cf = (CallFrame) evt.getNewValue();
            if (cf != null) {
                Script script = cf.getScript();
                if (script != null) {
                    Project project = pc != null ? pc.getProject() : null;
                    Debugger d = (Debugger) evt.getSource();
                    Line line = MiscEditorUtil.getLine(d, project, script,
                                                       cf.getLineNumber(),
                                                       cf.getColumnNumber());
                    MiscEditorUtil.showLine(line, true);
                }
            }
        }
        if (DebuggerManager.PROP_CURRENT_ENGINE.equals(propertyName)) {
            DebuggerEngine engine = (DebuggerEngine) evt.getNewValue();
            if (engine != null) {
                Debugger d = engine.lookupFirst("", Debugger.class);
                if (d != null) {
                    pc = engine.lookupFirst(null, ProjectContext.class);
                    List<CallFrame> stackTrace;
                    if (d.isSuspended()) {
                        stackTrace = d.getCurrentCallStack();
                    } else {
                        stackTrace = Collections.emptyList();
                    }
                    updateAnnotations(d, stackTrace);
                }
            }
        }
    }
    
    private void updateAnnotations(Debugger d, List<CallFrame> stackTrace) {
        for (Annotation ann : annotations) {
            ann.detach();
        }
        annotations.clear();
        boolean first = true;
        for (CallFrame cf : stackTrace) {
            Script script = cf.getScript();
            if (script == null) {
                continue;
            }
            Project project = pc != null ? pc.getProject() : null;
            final Line line = MiscEditorUtil.getLine(d, project, script,
                                                     cf.getLineNumber(),
                                                     cf.getColumnNumber());
            if (line == null) {
                first = false;
                continue;
            }
            Annotation anno;
            if (first) {
                anno = new CurrentLineAnnotation(line);
                MiscEditorUtil.showLine(line, true);
                first = false;
            } else {
                anno = new CallStackAnnotation(line);
            }
            annotations.add(anno);
        }
    }
    
    private void addDebuggerListener(Debugger d) {
        Debugger.Listener l = new DebuggerAnnotationsListener(d);
        synchronized (debuggerListeners) {
            debuggerListeners.put(d, l);
        }
        d.addListener(l);
    }
    
    private void removeDebuggerListener(Debugger d) {
        Debugger.Listener l;
        synchronized (debuggerListeners) {
            l = debuggerListeners.remove(d);
        }
        if (l != null) {
            d.removeListener(l);
        }
    }
    
    private class DebuggerAnnotationsListener implements Debugger.Listener {
        
        private final Debugger d;
        
        private DebuggerAnnotationsListener(Debugger d) {
            this.d = d;
        }
        
        @Override
        public void paused(List<CallFrame> callStack, String reason) {
            updateAnnotations(d, callStack);
        }

        @Override
        public void resumed() {
            updateAnnotations(null, Collections.<CallFrame>emptyList());
        }

        @Override
        public void reset() {}

        @Override
        public void enabled(boolean enabled) {}
        
    }

}
