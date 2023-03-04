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

package org.netbeans.modules.web.javascript.debugger.actions;

import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.web.javascript.debugger.MiscEditorUtil;
import org.netbeans.modules.web.webkit.debugging.api.Debugger;
import org.netbeans.modules.web.webkit.debugging.api.debugger.CallFrame;
import org.netbeans.modules.web.webkit.debugging.spi.JavaScriptDebuggerFactory;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.CodeEvaluator;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;


/**
 * This is the ActionsProvider for NetBeans JavaScript debugger.
 *
 * @author Sandip V. Chitale <sandipchitale@netbeans.org>
 */
@NbBundle.Messages({"MSG_WILL_PAUSE=The debugger will suspend on next JavaScript execution."})
@ActionsProvider.Registration(path="javascript-debuggerengine",
                              activateForMIMETypes={ MiscEditorUtil.JAVASCRIPT_MIME_TYPE,
                                                     MiscEditorUtil.HTML_MIME_TYPE,
                                                     MiscEditorUtil.PHP_MIME_TYPE })
public class DebuggerActionsProvider extends ActionsProviderSupport 
                                         implements Debugger.Listener {

    // Supported Actions
    private static final Set<Object> ACTIONS =
            Collections.unmodifiableSet(
                new HashSet<Object>(
                    Arrays.asList(new Object[] {
                        //ActionsManager.ACTION_START,
//                        ActionsManager.ACTION_KILL,
                        ActionsManager.ACTION_CONTINUE,
                        //ActionsManager.ACTION_PAUSE,
                        ActionsManager.ACTION_STEP_INTO,
                        ActionsManager.ACTION_STEP_OVER,
                        ActionsManager.ACTION_STEP_OUT,
//                        ActionsManager.ACTION_RUN_TO_CURSOR,
                        ActionsManager.ACTION_EVALUATE,
                    })));
    
    private static final Logger USG_LOGGER = Logger.getLogger("org.netbeans.ui.metrics.debugger"); // NOI18N
    private static final String USG_LOG_MSG = "USG_DEBUG_HTML5_JS";   // NOI18N

    private Debugger debugger;
    private static boolean usgLogged = false;

    private void updateDebuggerState() {
        if (!debugger.isEnabled()) {
//            setEnabled(ActionsManager.ACTION_START, false);
//            setEnabled(ActionsManager.ACTION_KILL, false);
            setEnabled(ActionsManager.ACTION_CONTINUE, false);
//            setEnabled(ActionsManager.ACTION_PAUSE, false);
            setEnabled(ActionsManager.ACTION_STEP_INTO, false);
            setEnabled(ActionsManager.ACTION_STEP_OVER, false);
            setEnabled(ActionsManager.ACTION_STEP_OUT, false);
            setEnabled(ActionsManager.ACTION_EVALUATE, false);
        } else if (debugger.isSuspended()) {
//            setEnabled(ActionsManager.ACTION_START, false);
//            setEnabled(ActionsManager.ACTION_KILL, true);
            setEnabled(ActionsManager.ACTION_CONTINUE, true);
//            setEnabled(ActionsManager.ACTION_PAUSE, false);
            setEnabled(ActionsManager.ACTION_STEP_INTO, true);
            setEnabled(ActionsManager.ACTION_STEP_OVER, true);
            setEnabled(ActionsManager.ACTION_STEP_OUT, true);
            setEnabled(ActionsManager.ACTION_EVALUATE, true);
        } else {
//            setEnabled(ActionsManager.ACTION_START, false);
//            setEnabled(ActionsManager.ACTION_KILL, true);
            setEnabled(ActionsManager.ACTION_CONTINUE, false);
//            setEnabled(ActionsManager.ACTION_PAUSE, true);
            setEnabled(ActionsManager.ACTION_STEP_INTO, false);
            setEnabled(ActionsManager.ACTION_STEP_OVER, false);
            setEnabled(ActionsManager.ACTION_STEP_OUT, false);
            setEnabled(ActionsManager.ACTION_EVALUATE, false);
        }
    }

   // private JSDebuggerEventListener debuggerListener;

    public DebuggerActionsProvider(ContextProvider contextProvider) {
        debugger = contextProvider.lookupFirst(null, Debugger.class);
        debugger.addListener(this);
//        contextProviderWrapper = ContextProviderWrapper.getContextProviderWrapper(contextProvider);
//        debugger = contextProviderWrapper.getNbJSDebugger();

        // Initially enabled actions - ACTION_START
        //setEnabled(ActionsManager.ACTION_START, true);
        //setEnabled(ActionsManager.ACTION_KILL, true);

        // Add listener to JSDebugger
        //debuggerListener = new JSDebuggerEventListenerImpl();
//        debugger.addJSDebuggerEventListener(WeakListeners.create(
//                JSDebuggerEventListener.class,
//                debuggerListener,
//                debugger));
        //EditorContextDispatcher ctxtDispatcher = EditorContextDispatcher.getDefault();
        //ctxtDispatcher.addPropertyChangeListener(WeakListeners.propertyChange(this, ctxtDispatcher));
    }

    @Override
    public Set<Object> getActions() {
        return ACTIONS;
    }

    @Override
    public void postAction(Object action, Runnable actionPerformedNotifier) {
        if (action == ActionsManager.ACTION_EVALUATE) {
            CodeEvaluator.getDefault().open();
            actionPerformedNotifier.run();
        } else {
            super.postAction(action, actionPerformedNotifier);
        }
    }
    
    @Override
    public void doAction(final Object action) {
        if (!usgLogged) {
            logJSDebugAction();
            usgLogged = true;
        }
        if (action == ActionsManager.ACTION_START) {
            //debugger.startJSDebugging();
        } else if (action == ActionsManager.ACTION_KILL) {
            if (debugger.isEnabled()) {
                JavaScriptDebuggerFactory factory =
                        Lookup.getDefault().lookup(JavaScriptDebuggerFactory.class);
                if (factory != null) {
                    factory.stopDebuggingSession(DebuggerManager.getDebuggerManager().getCurrentSession());
                }
            }
        } else if (action == ActionsManager.ACTION_CONTINUE) {
            debugger.resume();
        } else if (action == ActionsManager.ACTION_PAUSE) {
            debugger.pause();
            StatusDisplayer.getDefault().setStatusText(
                    Bundle.MSG_WILL_PAUSE());
            setEnabled(ActionsManager.ACTION_PAUSE, false);
        } else if (action == ActionsManager.ACTION_STEP_OVER) {
            debugger.stepOver();
        } else if (action == ActionsManager.ACTION_STEP_INTO) {
            debugger.stepInto();
        } else if (action == ActionsManager.ACTION_STEP_OUT) {
            debugger.stepOut();
        } else if (action == ActionsManager.ACTION_RUN_TO_CURSOR) {
            //debugger.runToCursor();
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        handleRunToCursor();
    }

    private void handleRunToCursor() {
        JEditorPane editorPane = EditorContextDispatcher.getDefault().getCurrentEditor();
        setEnabled (
            ActionsManager.ACTION_RUN_TO_CURSOR,
            editorPane != null &&
            getActionsManager().isEnabled(ActionsManager.ACTION_CONTINUE) &&
            EditorContextDispatcher.getDefault().getCurrentLineNumber () >= 0 &&
            MiscEditorUtil.isJSOrWrapperMIMEType(editorPane.getContentType())
        );
    }
    
    static ActionsManager getActionsManager () {
        return DebuggerManager.getDebuggerManager ().
            getCurrentEngine () == null ? 
            DebuggerManager.getDebuggerManager ().getActionsManager () :
            DebuggerManager.getDebuggerManager ().getCurrentEngine ().
                getActionsManager ();
    }

    @Override
    public void paused(List<CallFrame> callStack, String reason) {
        updateDebuggerState();
    }

    @Override
    public void resumed() {
        updateDebuggerState();
    }

    @Override
    public void reset() {
    }

    @Override
    public void enabled(boolean enabled) {
        updateDebuggerState();
    }
    
    private void logJSDebugAction() {
        LogRecord record = new LogRecord(Level.INFO, USG_LOG_MSG);
        record.setResourceBundle(NbBundle.getBundle(DebuggerActionsProvider.class));
        record.setResourceBundleName(DebuggerActionsProvider.class.getPackage().getName() + ".Bundle"); // NOI18N
        record.setLoggerName(USG_LOGGER.getName());
        boolean inLiveHTML = debugger.isInLiveHTMLMode();
        record.setParameters(new Object[] { inLiveHTML });
        USG_LOGGER.log(record);
    }

}
