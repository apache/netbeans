/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
