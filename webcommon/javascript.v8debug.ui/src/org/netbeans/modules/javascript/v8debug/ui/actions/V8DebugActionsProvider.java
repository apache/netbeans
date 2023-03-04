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

package org.netbeans.modules.javascript.v8debug.ui.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import static org.netbeans.api.debugger.ActionsManager.*;
import org.netbeans.modules.javascript.v8debug.V8Debugger;
import org.netbeans.modules.javascript.v8debug.V8DebuggerSessionProvider;
import org.netbeans.modules.javascript.v8debug.frames.CallFrame;
import org.netbeans.modules.javascript2.debug.ui.JSUtils;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.CodeEvaluator;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.filesystems.FileObject;
import org.openide.text.Line;

/**
 *
 * @author Martin Entlicher
 */
@ActionsProvider.Registration(path=V8DebuggerSessionProvider.SESSION_NAME)
public class V8DebugActionsProvider extends ActionsProviderSupport implements V8Debugger.Listener {
    
    private static final Logger LOG = Logger.getLogger(V8DebugActionsProvider.class.getName());

    private static final Set<Object> ACTIONS =
            Collections.unmodifiableSet(
                new HashSet<>(
                    Arrays.asList(new Object[] {
                        ACTION_RUN_TO_CURSOR,
                        ACTION_EVALUATE,
                    })));
    
    private final V8Debugger dbg;
    private final PropertyChangeListener jsFileContextListener = new JSFileContextListener();
    
    public V8DebugActionsProvider(ContextProvider contextProvider) {
        dbg = contextProvider.lookupFirst(null, V8Debugger.class);
        dbg.addListener(this);
        notifySuspended(false);
    }

    @Override
    public void postAction(Object action, final Runnable actionPerformedNotifier) {
        if (action == ACTION_EVALUATE) {
            CodeEvaluator.getDefault().open();
            actionPerformedNotifier.run();
        } else {
            super.postAction(action, actionPerformedNotifier);
        }
    }
    
    @Override
    public void doAction(Object action) {
        LOG.fine("doAction("+action+")");
        if (action == ACTION_RUN_TO_CURSOR) {
            Line currentLine = JSUtils.getCurrentLine();
            if (currentLine != null) {
                FileObject fo = currentLine.getLookup().lookup(FileObject.class);
                long line = (long) currentLine.getLineNumber();
                dbg.runTo(fo, line);
            }
        }
    }

    @Override
    public Set getActions() {
        return ACTIONS;
    }

    @Override
    public void notifySuspended(boolean suspended) {
        setEnabled(ACTION_EVALUATE, suspended);
        if (suspended) {
            EditorContextDispatcher.getDefault().addPropertyChangeListener(JSUtils.JS_MIME_TYPE, jsFileContextListener);
            setEnabled(ACTION_RUN_TO_CURSOR, JSUtils.getCurrentLine() != null);
        } else {
            EditorContextDispatcher.getDefault().removePropertyChangeListener(jsFileContextListener);
            setEnabled(ACTION_RUN_TO_CURSOR, false);
        }
    }

    @Override
    public void notifyCurrentFrame(CallFrame cf) {
    }
    
    @Override
    public void notifyFinished() {
        EditorContextDispatcher.getDefault().removePropertyChangeListener(jsFileContextListener);
    }
    
    private class JSFileContextListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            setEnabled(ACTION_RUN_TO_CURSOR, JSUtils.getCurrentLine() != null);
        }
        
    }
    
}
