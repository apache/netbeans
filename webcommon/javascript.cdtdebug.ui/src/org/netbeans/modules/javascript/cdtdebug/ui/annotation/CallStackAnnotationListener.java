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

package org.netbeans.modules.javascript.cdtdebug.ui.annotation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.lib.chrome_devtools_protocol.debugger.CallFrame;
import org.netbeans.modules.javascript.cdtdebug.CDTDebugger;
import org.netbeans.modules.javascript.cdtdebug.CDTScript;
import org.netbeans.modules.javascript.cdtdebug.ScriptsHandler;
import org.netbeans.modules.javascript.cdtdebug.ui.EditorUtils;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.filesystems.FileObject;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.util.RequestProcessor;

@DebuggerServiceRegistration(types=LazyDebuggerManagerListener.class)
public final class CallStackAnnotationListener extends DebuggerManagerAdapter
                                               implements PropertyChangeListener {

    private static final Logger LOG = Logger.getLogger(CallStackAnnotationListener.class.getName());

    private final List<Annotation> annotations = new ArrayList<>();
    private final Map<CDTDebugger, CDTDebugger.Listener> dbgListeners = new HashMap<>();
    private volatile CDTDebugger currentCDTDbg;
    private final RequestProcessor annotationProcessor = new RequestProcessor(CallStackAnnotationListener.class);

    @Override
    public String[] getProperties() {
        return new String[] { DebuggerManager.PROP_DEBUGGER_ENGINES,
                              DebuggerManager.PROP_CURRENT_ENGINE };
    }

    @Override
    public void engineAdded(DebuggerEngine engine) {
        CDTDebugger dbg = engine.lookupFirst(null, CDTDebugger.class);
        if (dbg != null) {
            DbgListener l = new DbgListener(dbg);
            dbgListeners.put(dbg, l);
            dbg.addListener(l);
            if (dbg == currentCDTDbg) {
                annotationProcessor.post(new AnnotateTask(dbg));
            }
        }
    }

    @Override
    public void engineRemoved(DebuggerEngine engine) {
        CDTDebugger dbg = engine.lookupFirst(null, CDTDebugger.class);
        if (dbg != null) {
            CDTDebugger.Listener l = dbgListeners.remove(dbg);
            dbg.removeListener(l);
            if (dbg == currentCDTDbg) {
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
                    CDTDebugger dbg = engine.lookupFirst(null, CDTDebugger.class);
                    currentCDTDbg = dbg;
                    annotationProcessor.post(new AnnotateTask(dbg));
                }
                break;
        }
    }

    private void updateAnnotations(CDTDebugger dbg) {
        clearAnnotations();
        List<CallFrame> cs;
        if (dbg.isSuspended()) {
            cs = dbg.getCurrentCallStack();
            LOG.log(Level.FINE, "updateAnnotations(), dbg IS suspended, call stack = {0}", cs);
        } else {
            LOG.fine("updateAnnotations(), dbg is NOT suspended.");
            return ;
        }
        if (cs != null && !cs.isEmpty()) {
            List<Annotation> newAnnotations = new ArrayList<>();
            ScriptsHandler scriptsHandler = dbg.getScriptsHandler();
            boolean first = true;
            for (CallFrame f : cs) {
                CDTScript script = dbg.getScriptsHandler().getScript(f.getLocation().getScriptId());
                if (script == null) {
                    continue;
                }
                FileObject fo = scriptsHandler.getFile(script);
                Line line = EditorUtils.getLine(dbg, fo,
                        (int) f.getLocation().getLineNumber(),
                        f.getLocation().getColumnNumber() != null ? f.getLocation().getColumnNumber() : 0
                );
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
                LOG.log(Level.FINE, "Created and stored {0} annotations.", newAnnotations.size());
                annotations.addAll(newAnnotations);
            }
        }
    }

    private void clearAnnotations() {
        synchronized (annotations) {
            LOG.log(Level.FINE, "Clearing {0} annotations.", annotations.size());
            for (Annotation ann : annotations) {
                ann.detach();
            }
            annotations.clear();
        }
    }

    private final class DbgListener implements CDTDebugger.Listener {

        private final CDTDebugger dbg;
        private volatile boolean topFrameShown;

        public DbgListener(CDTDebugger dbg) {
            this.dbg = dbg;
        }

        @Override
        public void notifySuspended(boolean suspended) {
            if (currentCDTDbg == dbg) {
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
            if (topFrameShown && cf == dbg.getCurrentCallStack().get(0)) {
                return ;
            }
            topFrameShown = false;
            annotationProcessor.execute(() -> EditorUtils.showFrameLine(dbg, cf, true));
        }

        @Override
        public void notifyFinished() {
        }

    }

    private final class AnnotateTask implements Runnable {

        private final CDTDebugger dbg;

        public AnnotateTask(CDTDebugger dbg) {
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
