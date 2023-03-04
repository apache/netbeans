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

package org.netbeans.modules.web.javascript.debugger.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;
import org.netbeans.modules.javascript2.debug.ui.JSUtils;
import org.netbeans.modules.web.javascript.debugger.MiscEditorUtil;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.filesystems.FileObject;
import org.openide.text.Line;
import org.openide.util.WeakListeners;

/**
 *
 */
@ActionsProvider.Registration(actions={"toggleBreakpoint"}, 
        //activateForMIMETypes={MiscEditorUtil.JAVASCRIPT_MIME_TYPE, MiscEditorUtil.HTML_MIME_TYPE})
        activateForMIMETypes={ MiscEditorUtil.HTML_MIME_TYPE, MiscEditorUtil.PHP_MIME_TYPE })
public class BreakpointActionProvider extends ActionsProviderSupport
        implements PropertyChangeListener 
{

    public BreakpointActionProvider() {
        setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT, false);
//        EditorContextDispatcher.getDefault().addPropertyChangeListener(
//                MiscEditorUtil.JAVASCRIPT_MIME_TYPE,
//                WeakListeners.propertyChange(this, EditorContextDispatcher.getDefault()));
        EditorContextDispatcher.getDefault().addPropertyChangeListener(
                MiscEditorUtil.HTML_MIME_TYPE,
                WeakListeners.propertyChange(this, EditorContextDispatcher.getDefault()));
        EditorContextDispatcher.getDefault().addPropertyChangeListener(
                MiscEditorUtil.PHP_MIME_TYPE,
                WeakListeners.propertyChange(this, EditorContextDispatcher.getDefault()));
    }

    @Override
    public void doAction(Object action) {
        if (SwingUtilities.isEventDispatchThread()) {
            addBreakpoints();
        }
        else {
            SwingUtilities.invokeLater( new Runnable() {
                @Override
                public void run() {
                    addBreakpoints();
                } 
            });
        }
    }

    @Override
    public Set getActions() {
        return Collections.singleton(ActionsManager.ACTION_TOGGLE_BREAKPOINT );
    }
    
    private void addBreakpoints() {
        Line line = MiscEditorUtil.getCurrentLine();

        if (line == null) {
            return;
        }

        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager()
                .getBreakpoints();
        boolean add = true;
        for ( Breakpoint breakpoint : breakpoints ) {
            if (breakpoint instanceof JSLineBreakpoint
                    && JSUtils.getLine((JSLineBreakpoint) breakpoint).equals(line)  )
            {
                DebuggerManager.getDebuggerManager().removeBreakpoint(
                        breakpoint );
                add = false;
                break;
            }
        }
        add = add && MiscEditorUtil.isInJavaScript(line);
        if ( add ) {
            DebuggerManager.getDebuggerManager().addBreakpoint(
                    JSUtils.createLineBreakpoint(line));
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // We need to push the state there :-(( instead of wait for someone to be interested in...
        FileObject fo = EditorContextDispatcher.getDefault().getCurrentFile();
        setEnabled (
            ActionsManager.ACTION_TOGGLE_BREAKPOINT,
            (fo != null && MiscEditorUtil.isJSWrapperSource(fo))
        );
    }

}
