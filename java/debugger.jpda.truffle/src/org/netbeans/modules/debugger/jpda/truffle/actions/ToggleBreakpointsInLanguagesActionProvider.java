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

package org.netbeans.modules.debugger.jpda.truffle.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.debugger.jpda.truffle.MIMETypes;
import org.netbeans.modules.debugger.jpda.truffle.breakpoints.TruffleLineBreakpoint;
import org.netbeans.modules.javascript2.debug.EditorLineHandlerFactory;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.filesystems.FileObject;
import org.openide.text.Line;
import org.openide.util.WeakListeners;

/**
 * Toggle breakpoint in the guest language.
 */
@ActionsProvider.Registration(path="", actions={ "toggleBreakpoint" })
public class ToggleBreakpointsInLanguagesActionProvider extends ActionsProviderSupport
                                                        implements PropertyChangeListener {
    
    private static final Set<String> IGNORED_MIME_TYPES = new HashSet<>(
            // We have JSLineBreakpoint in JavaScript
            Arrays.asList("text/javascript", "text/x-java", "text/x-groovy"));  // NOI18N
    
    private volatile Line postedLine;
    
    public ToggleBreakpointsInLanguagesActionProvider() {
        setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT, false);
        EditorContextDispatcher.getDefault().addPropertyChangeListener(
                WeakListeners.propertyChange(this, EditorContextDispatcher.getDefault()));
        MIMETypes.getDefault().addPropertyChangeListener(
                WeakListeners.propertyChange(this, MIMETypes.getDefault()));
    }

    @Override
    public void postAction(Object action, final Runnable actionPerformedNotifier) {
        assert action == ActionsManager.ACTION_TOGGLE_BREAKPOINT : action;
        EditorContextDispatcher context = EditorContextDispatcher.getDefault();
        postedLine = context.getCurrentLine();
        if (postedLine == null) {
            actionPerformedNotifier.run();
            return ;
        }
        super.postAction(action, new Runnable() {
            @Override
            public void run() {
                try {
                    actionPerformedNotifier.run();
                } finally {
                    postedLine = null;
                }
            }
        });
    }
    
    @Override
    public void doAction(Object action) {
        assert action == ActionsManager.ACTION_TOGGLE_BREAKPOINT : action;
        DebuggerManager d = DebuggerManager.getDebuggerManager ();
        Line line = postedLine;
        if (line == null) {
            line = EditorContextDispatcher.getDefault().getCurrentLine();
            if (line == null) {
                return ;
            }
        }
        FileObject fo = line.getLookup().lookup(FileObject.class);
        if (fo == null) {
            return ;
        }
        if (IGNORED_MIME_TYPES.contains(fo.getMIMEType())) {
            return ;
        }
        Set<String> mts = MIMETypes.getDefault().get();
        if (!mts.contains(fo.getMIMEType())) {
            return ;
        }
        toggleBreakpoint(fo, line);
    }

    @Override
    public Set getActions() {
        return Collections.singleton (ActionsManager.ACTION_TOGGLE_BREAKPOINT);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if (MIMETypes.PROP_MIME_TYPES.equals(propertyName)) {
            // Platform MIME types changed, enable the action
            // and load them when the action is invoked.
            setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT, true);
        } else if (EditorContextDispatcher.PROP_FILE.equals(propertyName)) {
            boolean enabled = false;
            FileObject fo = EditorContextDispatcher.getDefault().getCurrentFile();
            if (fo != null && !IGNORED_MIME_TYPES.contains(fo.getMIMEType())) {
                Set<String> mts = MIMETypes.getDefault().getCached();
                if (mts == null || mts.contains(fo.getMIMEType())) {
                    // When MIME types are not loaded yet, enable the action
                    // and load them when the action is invoked.
                    enabled = true;
                }
            }
            setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT, enabled);
        }
    }
    
    private void toggleBreakpoint(FileObject fo, Line line) {
        DebuggerManager d = DebuggerManager.getDebuggerManager();
        boolean add = true;
        int lineNumber = line.getLineNumber() + 1;
        for (Breakpoint breakpoint : d.getBreakpoints()) {
            if (breakpoint instanceof TruffleLineBreakpoint &&
                ((TruffleLineBreakpoint) breakpoint).getFileObject().equals(fo) &&
                ((TruffleLineBreakpoint) breakpoint).getLineNumber() == lineNumber) {
                
                d.removeBreakpoint(breakpoint);
                add = false;
                break;
            }
        }
        if (add) {
            d.addBreakpoint(createLineBreakpoint(line));
        }
        
    }

    private Breakpoint createLineBreakpoint(Line line) {
        FileObject fo = line.getLookup().lookup(FileObject.class);
        return new TruffleLineBreakpoint(EditorLineHandlerFactory.getHandler(fo, line.getLineNumber() + 1));
    }
    
}
