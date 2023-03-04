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

package org.netbeans.modules.cpplite.debugger.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.spi.debugger.ActionsProvider.Registration;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.filesystems.FileObject;
import org.openide.text.Line;
import org.openide.util.WeakListeners;

/**
 *
 * @author  Honza
 */
@Registration(actions={"toggleBreakpoint"}, activateForMIMETypes={"text/X-c", "text/X-c++", "text/X-h", "text/X-h++"})
public class CPPLiteBreakpointActionProvider extends ActionsProviderSupport
                                         implements PropertyChangeListener {

    private static final String[] C_MIME_TYPES = new String[] {"text/X-c", "text/X-c++", "text/X-h", "text/X-h++"}; // NOI18N
    private static final Set<String> C_MIME_TYPES_SET = new HashSet<>(Arrays.asList(C_MIME_TYPES));

    private static final Set ACTIONS = Collections.singleton (
        ActionsManager.ACTION_TOGGLE_BREAKPOINT
    );

    EditorContextDispatcher context = EditorContextDispatcher.getDefault();

    public CPPLiteBreakpointActionProvider () {
        for (String mimeType : C_MIME_TYPES) {
            context.addPropertyChangeListener(mimeType,
                    WeakListeners.propertyChange(this, context));
        }
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
            if (breakpoints[i] instanceof CPPLiteBreakpoint) {
                CPPLiteBreakpoint cppb = (CPPLiteBreakpoint) breakpoints[i];
                if (fo.equals(cppb.getFileObject()) && cppb.getLineNumber() == lineNumber) {
                    DebuggerManager.getDebuggerManager().removeBreakpoint(cppb);
                    break;
                }
            }
        }
        if (i == k) {
            DebuggerManager.getDebuggerManager ().addBreakpoint (
                CPPLiteBreakpoint.create(line)
            );
        }
    }

    /**
     * Returns set of actions supported by this ActionsProvider.
     *
     * @return set of actions supported by this ActionsProvider
     */
    @Override
    public Set getActions () {
        return ACTIONS;
    }

    private static Line getCurrentLine () {
        FileObject fo = EditorContextDispatcher.getDefault().getCurrentFile();
        //System.out.println("n = "+n+", FO = "+fo+" => is ANT = "+isAntFile(fo));
        if (!isCFile(fo)) {
            return null;
        }
        return EditorContextDispatcher.getDefault().getCurrentLine();
    }

    private static boolean isCFile(FileObject fo) {
        if (fo == null) {
            return false;
        } else {
            return C_MIME_TYPES_SET.contains(fo.getMIMEType());
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // We need to push the state there :-(( instead of wait for someone to be interested in...
        boolean enabled = getCurrentLine() != null;
        setEnabled (ActionsManager.ACTION_TOGGLE_BREAKPOINT, enabled);
    }

}
