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

package org.netbeans.modules.debugger.jpda.projectsui;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.project.Project;
import org.netbeans.spi.debugger.ActionsProvider.Registration;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;


/**
*
* @author   Jan Jancura
*/
@Registration(actions={"runToCursor"}, activateForMIMETypes={"text/x-java"})
public class RunToCursorActionProvider extends ActionsProviderSupport {
    
    private static final RequestProcessor RP = new RequestProcessor(RunToCursorActionProvider.class.getName());
    
    private final EditorContextDispatcher editorContext;
    private final Map<Project, LineBreakpoint> projectBreakpoints = new HashMap<>();
    
    {
        editorContext = EditorContextDispatcher.getDefault();
        
        Listener listener = new Listener ();
        MainProjectManager.getDefault ().addPropertyChangeListener (listener);
        editorContext.addPropertyChangeListener("text/x-java",
                WeakListeners.propertyChange(listener, editorContext));
        DebuggerManager.getDebuggerManager ().addDebuggerListener (
            DebuggerManager.PROP_DEBUGGER_ENGINES,
            listener
        );

        setEnabled (
            ActionsManager.ACTION_RUN_TO_CURSOR,
            shouldBeEnabled ()
        );
    }
    
    @Override
    public Set getActions () {
        return Collections.singleton (ActionsManager.ACTION_RUN_TO_CURSOR);
    }
    
    @Override
    public void doAction (Object action) {
        
        // 1) set breakpoint
        //removeBreakpoint ();
        LineBreakpoint newBreakpoint = LineBreakpoint.create (
                editorContext.getCurrentURLAsString(),
                editorContext.getCurrentLineNumber ()
            );
        createBreakpoint (newBreakpoint);
        
        // 2) start debugging of project
        invokeAction(newBreakpoint);
    }
    
    @Override
    public void postAction(Object action, final Runnable actionPerformedNotifier) {
        final LineBreakpoint newBreakpoint = LineBreakpoint.create (
            editorContext.getCurrentURLAsString(),
            editorContext.getCurrentLineNumber ()
        );
        // Disable the action immediatelly, to prevent multiple action invocations.
        setEnabled (
            ActionsManager.ACTION_RUN_TO_CURSOR,
            false
        );
        RP.post(new Runnable() {
            @Override
            public void run() {
                // 1) set breakpoint
                //removeBreakpoint ();
                createBreakpoint (newBreakpoint);
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            invokeAction(newBreakpoint);
                        }
                    });
                } catch (InterruptedException iex) {
                    // Procceed
                } catch (java.lang.reflect.InvocationTargetException itex) {
                    ErrorManager.getDefault().notify(itex);
                } finally {
                    actionPerformedNotifier.run();
                    setEnabled (
                        ActionsManager.ACTION_RUN_TO_CURSOR,
                        shouldBeEnabled ()
                    );
                }
            }
        });
    }
    
    private void invokeAction(LineBreakpoint newBreakpoint) {
        debugProject(MainProjectManager.getDefault().getMainProject(), newBreakpoint);
    }

    private void debugProject(final Project p, LineBreakpoint newBreakpoint) {
        synchronized (projectBreakpoints) {
            projectBreakpoints.put(p, newBreakpoint);
        }
        ActionProgress progress = new ActionProgress() {

            @Override
            protected void started() {
            }

            @Override
            public void finished(boolean success) {
                LineBreakpoint lb;
                synchronized (projectBreakpoints) {
                    lb = projectBreakpoints.remove(p);
                }
                if (lb != null) {
                    removeBreakpoint(lb);
                }
                setEnabled (
                    ActionsManager.ACTION_RUN_TO_CURSOR,
                    shouldBeEnabled ()
                );
            }
        };
        p.getLookup ().lookup(ActionProvider.class).invokeAction (
                ActionProvider.COMMAND_DEBUG,
                new ProxyLookup(Lookups.fixed(progress), p.getLookup ())
            );
    }

    private boolean shouldBeEnabled () {
        if (editorContext.getCurrentLineNumber () < 0) {
            return false;
        }
        FileObject fo = editorContext.getCurrentFile();
        if (fo == null || !fo.hasExt("java")) {
            return false;
        }
        
        // check if current project supports this action
        Project p = MainProjectManager.getDefault ().getMainProject ();
        if (p == null) {
            return false;
        }
        synchronized (projectBreakpoints) {
            if (projectBreakpoints.containsKey(p)) {
                // Already debugging this project
                return false;
            }
        }
        ActionProvider actionProvider = (ActionProvider) p.getLookup ().
            lookup (ActionProvider.class);
        if (actionProvider == null) {
            return false;
        }
        String[] sa = actionProvider.getSupportedActions ();
        int i, k = sa.length;
        for (i = 0; i < k; i++) {
            if (ActionProvider.COMMAND_DEBUG.equals (sa [i])) {
                break;
            }
        }
        if (i == k) {
            return false;
        }

        // check if this action should be enabled
        return actionProvider.isActionEnabled (
                ActionProvider.COMMAND_DEBUG, 
                p.getLookup ()
            );
    }
    
    private void createBreakpoint (LineBreakpoint breakpoint) {
        breakpoint.setHidden (true);
        DebuggerManager.getDebuggerManager ().addBreakpoint (breakpoint);
    }
    
    private void removeBreakpoint (LineBreakpoint breakpoint) {
        if (breakpoint != null) {
            DebuggerManager.getDebuggerManager ().removeBreakpoint (breakpoint);
        }
    }
    
    private class Listener extends DebuggerManagerAdapter {
        
        private final Map<JPDADebugger, Pair<LineBreakpoint, Project>> debuggerBreakpoints = new HashMap<>();
        private final Map<JPDADebugger, Integer> debuggerEngines = new HashMap<>();
        
        @Override
        public void propertyChange (PropertyChangeEvent e) {
            if (e.getPropertyName () == JPDADebugger.PROP_STATE) {
                int state = ((Integer) e.getNewValue ()).intValue ();
                if ( (state == JPDADebugger.STATE_DISCONNECTED) ||
                     (state == JPDADebugger.STATE_STOPPED)
                ) {
                    JPDADebugger debugger = (JPDADebugger) e.getSource();
                    Pair<LineBreakpoint, Project> lbp;
                    synchronized (debuggerBreakpoints) {
                        lbp = debuggerBreakpoints.remove(debugger);
                    }
                    if (lbp != null) {
                        removeBreakpoint(lbp.first());
                        synchronized (projectBreakpoints) {
                            projectBreakpoints.put(lbp.second(), null);
                        }
                    }
                }
                return;
            }
            setEnabled (
                ActionsManager.ACTION_RUN_TO_CURSOR,
                shouldBeEnabled ()
            );
        }
        
        @Override
        public void engineAdded (DebuggerEngine engine) {
            JPDADebugger debugger = engine.lookupFirst(null, JPDADebugger.class);
            if (debugger == null) {
                return;
            }
            Integer num;
            synchronized (debuggerEngines) {
                num = debuggerEngines.get(debugger);
                if (num != null) {
                    debuggerEngines.put(debugger, num+1);
                    return ;
                } else {
                    debuggerEngines.put(debugger, 1);
                }
            }
            //Project p = debuggedProject;
            Map setupMap = engine.lookupFirst(null, Map.class);
            if (setupMap != null) {
                File baseDir = (File) setupMap.get("baseDir");
                if (baseDir != null) {
                    Project prj = null;
                    LineBreakpoint lb = null;
                    synchronized (projectBreakpoints) {
                        for (Project p : projectBreakpoints.keySet()) {
                            if (baseDir.equals(FileUtil.toFile(p.getProjectDirectory()))) {
                                prj = p;
                                lb = projectBreakpoints.get(p);
                                break;
                            }
                        }
                    }
                    if (lb != null) {
                        synchronized (debuggerBreakpoints) {
                            debuggerBreakpoints.put(debugger, Pair.of(lb, prj));
                        }
                    }
                }
            }
            //debuggingProjects.put(p, debugger);
            debugger.addPropertyChangeListener (
                JPDADebugger.PROP_STATE,
                this
            );
        }
        
        @Override
        public void engineRemoved (DebuggerEngine engine) {
            JPDADebugger debugger = engine.lookupFirst(null, JPDADebugger.class);
            if (debugger == null) {
                return;
            }
            synchronized (debuggerEngines) {
                Integer num = debuggerEngines.get(debugger);
                if (num != null) {
                    if (num > 1) {
                        debuggerEngines.put(debugger, num-1);
                        return ;
                    } else {
                        debuggerEngines.remove(debugger);
                    }
                }
            }
            debugger.removePropertyChangeListener (
                JPDADebugger.PROP_STATE,
                this
            );
        }
    }
}
