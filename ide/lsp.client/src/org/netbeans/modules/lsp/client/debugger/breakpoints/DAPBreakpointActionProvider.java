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

package org.netbeans.modules.lsp.client.debugger.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.lsp.client.debugger.api.RegisterDAPBreakpoints;
import org.netbeans.spi.debugger.ActionsProvider.Registration;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.Line;
import org.openide.util.Lookup.Result;
import org.openide.util.WeakListeners;

@Registration(actions={"toggleBreakpoint"})
public final class DAPBreakpointActionProvider
extends ActionsProviderSupport implements PropertyChangeListener {

    private static final Set ACTIONS = Collections.singleton (
        ActionsManager.ACTION_TOGGLE_BREAKPOINT
    );

    private record BreakpointInfo(boolean dapBreakpointsAllowed,
                                  Result<RegisterDAPBreakpoints> registerLookup) {}
    private static final Map<String, BreakpointInfo> mimeType2BreakpointInfo = new HashMap<>();

    private static boolean hasMimeTypeDAPBreakpoints(String mimeType) {
        if(mimeType == null) {
            return false;
        }
        synchronized (mimeType2BreakpointInfo) {
            return mimeType2BreakpointInfo.computeIfAbsent(mimeType, mt -> {
                Result<RegisterDAPBreakpoints> result = MimeLookup.getLookup(mimeType).lookupResult(RegisterDAPBreakpoints.class);
                result.addLookupListener(evt -> {
                    synchronized (mimeType2BreakpointInfo) {
                        mimeType2BreakpointInfo.put(mimeType, new BreakpointInfo(!result.allInstances().isEmpty(), result));
                    }
                });
                return new BreakpointInfo(!result.allInstances().isEmpty(), result);
            }).dapBreakpointsAllowed();
        }
    }

    private EditorContextDispatcher context = EditorContextDispatcher.getDefault();

    public DAPBreakpointActionProvider () {
        context.addPropertyChangeListener(
                WeakListeners.propertyChange(this, context));
        setEnabled (ActionsManager.ACTION_TOGGLE_BREAKPOINT, false);
    }

    /**
     * Called when the action is called (action button is pressed).
     *
     * @param action an action which has been called
     */
    @Override
    public void doAction (Object action) {
        Line line = getCurrentLine ();
        if (line == null) {
            return ;
        }
        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints ();
        FileObject fo = line.getLookup().lookup(FileObject.class);
        if (fo == null) {
            return ;
        }
        int lineNumber = line.getLineNumber() + 1;
        int i, k = breakpoints.length;
        for (i = 0; i < k; i++) {
            if (breakpoints[i] instanceof DAPLineBreakpoint lb) {
                if (fo.equals(lb.getFileObject()) && lb.getLineNumber() == lineNumber) {
                    DebuggerManager.getDebuggerManager().removeBreakpoint(lb);
                    break;
                }
            }
        }
        if (i == k) {
            DebuggerManager.getDebuggerManager ().addBreakpoint (
                DAPLineBreakpoint.create(line)
            );
        }
    }

    /**
     * Returns set of actions supported by this ActionsProvider.
     *
     * @return set of actions supported by this ActionsProvider
     */
    @Override
    public Set<Object> getActions () {
        return ACTIONS;
    }

    private static Line getCurrentLine () {
        FileObject fo = EditorContextDispatcher.getDefault().getCurrentFile();
        //System.out.println("n = "+n+", FO = "+fo+" => is ANT = "+isAntFile(fo));
        if (!isRelevantFile(fo)) {
            return null;
        }
        return EditorContextDispatcher.getDefault().getCurrentLine();
    }

    private static boolean isRelevantFile(FileObject fo) {
        if (fo == null) {
            return false;
        } else {
            return hasMimeTypeDAPBreakpoints(FileUtil.getMIMEType(fo));
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // We need to push the state there :-(( instead of wait for someone to be interested in...
        boolean enabled = getCurrentLine() != null;
        setEnabled (ActionsManager.ACTION_TOGGLE_BREAKPOINT, enabled);
    }

}
