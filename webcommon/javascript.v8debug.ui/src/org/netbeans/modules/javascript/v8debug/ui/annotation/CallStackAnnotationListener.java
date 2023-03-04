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

package org.netbeans.modules.javascript.v8debug.ui.annotation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.lib.v8debug.V8Frame;
import org.netbeans.lib.v8debug.V8Script;
import org.netbeans.modules.javascript.v8debug.ui.EditorUtils;
import org.netbeans.modules.javascript.v8debug.ScriptsHandler;
import org.netbeans.modules.javascript.v8debug.V8Debugger;
import org.netbeans.modules.javascript.v8debug.frames.CallFrame;
import org.netbeans.modules.javascript.v8debug.frames.CallStack;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.filesystems.FileObject;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Martin Entlicher
 */
@DebuggerServiceRegistration(types=LazyDebuggerManagerListener.class)
public final class CallStackAnnotationListener extends DebuggerManagerAdapter
                                               implements PropertyChangeListener {
    
    private static final Logger LOG = Logger.getLogger(CallStackAnnotationListener.class.getName());
    
    private final List<Annotation> annotations = new ArrayList<>();
    private final Map<V8Debugger, V8Debugger.Listener> dbgListeners = new HashMap<>();
    private volatile V8Debugger currentV8Dbg;
    private final RequestProcessor annotationProcessor = new RequestProcessor(CallStackAnnotationListener.class);
    
    @Override
    public String[] getProperties() {
        return new String[] { DebuggerManager.PROP_DEBUGGER_ENGINES,
                              DebuggerManager.PROP_CURRENT_ENGINE };
    }
    
    @Override
    public void engineAdded(DebuggerEngine engine) {
        V8Debugger dbg = engine.lookupFirst(null, V8Debugger.class);
        if (dbg != null) {
            DbgListener l = new DbgListener(dbg);
            dbgListeners.put(dbg, l);
            dbg.addListener(l);
            if (dbg == currentV8Dbg) {
                annotationProcessor.post(new AnnotateTask(dbg));
            }
        }
    }

    @Override
    public void engineRemoved(DebuggerEngine engine) {
        V8Debugger dbg = engine.lookupFirst(null, V8Debugger.class);
        if (dbg != null) {
            V8Debugger.Listener l = dbgListeners.remove(dbg);
            dbg.removeListener(l);
            if (dbg == currentV8Dbg) {
                annotationProcessor.post(new AnnotateTask(null));
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case DebuggerManager.PROP_CURRENT_ENGINE:
                DebuggerEngine engine = (DebuggerEngine) evt.getNewValue();
                if (engine != null) {
                    V8Debugger dbg = engine.lookupFirst(null, V8Debugger.class);
                    currentV8Dbg = dbg;
                    annotationProcessor.post(new AnnotateTask(dbg));
                }
                break;
        }
    }
    
    private void updateAnnotations(V8Debugger dbg) {
        clearAnnotations();
        CallStack cs;
        if (dbg.isSuspended()) {
            cs = dbg.getCurrentCallStack();
            LOG.fine("updateAnnotations(), dbg IS suspended, call stack = "+cs);
        } else {
            LOG.fine("updateAnnotations(), dbg is NOT suspended.");
            return ;
        }
        if (cs != null && !cs.isEmpty()) {
            List<Annotation> newAnnotations = new ArrayList<>();
            ScriptsHandler scriptsHandler = dbg.getScriptsHandler();
            boolean first = true;
            for (V8Frame f : cs.getFrames()) {
                V8Script script = cs.getScript(f);
                if (script == null) {
                    continue;
                }
                FileObject fo = scriptsHandler.getFile(script);
                Line line = EditorUtils.getLine(dbg, fo, (int) f.getLine(), (int) f.getColumn());
                if (line == null) {
                    first = false;
                    continue;
                }
                Annotation anno;
                if (first) {
                    anno = new CurrentLineAnnotation(line);
                    EditorUtils.showLine(line, true);
                    first = false;
                } else {
                    anno = new CallStackAnnotation(line);
                }
                newAnnotations.add(anno);
            }
            synchronized (annotations) {
                LOG.fine("Created and stored "+newAnnotations.size()+" annotations.");
                annotations.addAll(newAnnotations);
            }
        }
    }
    
    private void clearAnnotations() {
        synchronized (annotations) {
            LOG.fine("Clearing "+annotations.size()+" annotations.");
            for (Annotation ann : annotations) {
                ann.detach();
            }
            annotations.clear();
        }
    }
    
    private final class DbgListener implements V8Debugger.Listener {
        
        private final V8Debugger dbg;
        private volatile boolean topFrameShown;
        
        public DbgListener(V8Debugger dbg) {
            this.dbg = dbg;
        }

        @Override
        public void notifySuspended(boolean suspended) {
            if (currentV8Dbg == dbg) {
                if (suspended) {
                    annotationProcessor.post(new AnnotateTask(dbg));
                    topFrameShown = true;
                } else {
                    annotationProcessor.post(new AnnotateTask(null));
                    topFrameShown = false;
                }
            }
        }

        @Override
        public void notifyCurrentFrame(CallFrame cf) {
            if (cf == null) {
                return ;
            }
            if (topFrameShown && cf.isTopFrame()) {
                return ;
            }
            topFrameShown = false;
            EditorUtils.showFrameLine(dbg, cf, true);
        }
        
        @Override
        public void notifyFinished() {
        }
        
    }
    
    private final class AnnotateTask implements Runnable {
        
        private final V8Debugger dbg;
        
        public AnnotateTask(V8Debugger dbg) {
            this.dbg = dbg;
        }

        @Override
        public void run() {
            if (dbg != null) {
                updateAnnotations(dbg);
            } else {
                clearAnnotations();
            }
        }
        
    }
    
}
