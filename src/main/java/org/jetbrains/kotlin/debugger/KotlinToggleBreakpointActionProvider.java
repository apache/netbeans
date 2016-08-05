/**
 * *****************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************
 */
package org.jetbrains.kotlin.debugger;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.modules.debugger.jpda.EditorContextBridge;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Alexander.Baratynski
 */
@ActionsProvider.Registrations({
    @ActionsProvider.Registration(path="", actions={ "toggleBreakpoint" }, activateForMIMETypes={ "text/x-kt" }),
    @ActionsProvider.Registration(path="netbeans-JPDASession", actions={ "toggleBreakpoint" }, activateForMIMETypes={ "text/x-kt" })
})
public class KotlinToggleBreakpointActionProvider extends ActionsProviderSupport 
    implements PropertyChangeListener {

    private JPDADebugger debugger;
    
    public KotlinToggleBreakpointActionProvider() {
        EditorContextBridge.getContext().
                addPropertyChangeListener(KotlinToggleBreakpointActionProvider.this);
        setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT, true);
    }
    
    public KotlinToggleBreakpointActionProvider(ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
        debugger.addPropertyChangeListener(JPDADebugger.PROP_STATE, 
                KotlinToggleBreakpointActionProvider.this);
        setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT, true);
        EditorContextBridge.getContext().
                addPropertyChangeListener(KotlinToggleBreakpointActionProvider.this);
    }
    
    private void destroy() {
        debugger.removePropertyChangeListener(JPDADebugger.PROP_STATE, this);
        EditorContextBridge.getContext().removePropertyChangeListener(this);
    }
    
    @Override
    public void doAction(Object o) {
        DebuggerManager manager = DebuggerManager.getDebuggerManager();
        
        int lineNumber = EditorContextBridge.getContext().getCurrentLineNumber();
        String url = EditorContextBridge.getContext().getCurrentURL();
        
        if ("".equals(url.trim())) {
            return;
        }
        
        LineBreakpoint lineBreakpoint = findBreakpoint(url, lineNumber);
        if (lineBreakpoint != null) {
            manager.removeBreakpoint(lineBreakpoint);
            return;
        }
        
        lineBreakpoint = LineBreakpoint.create(url, lineNumber);
        lineBreakpoint.setPrintText("breakpoint");
        manager.addBreakpoint(lineBreakpoint);
    }

    @Override
    public Set getActions() {
        return Collections.singleton(ActionsManager.ACTION_TOGGLE_BREAKPOINT);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String url = EditorContextBridge.getContext().getCurrentURL();
        FileObject fo;
        try {
            fo = URLMapper.findFileObject(new URL(url));
        } catch (MalformedURLException ex) {
            fo = null;
        }
        setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT,
                (EditorContextBridge.getContext().getCurrentLineNumber() >= 0) &&
                        (fo != null && "text/x-kt".equals(fo.getMIMEType())));
        if (debugger != null && debugger.getState() == JPDADebugger.STATE_DISCONNECTED) {
            destroy();
        }
    }
    
    private static LineBreakpoint findBreakpoint(String url, int lineNumber) {
        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
        for (Breakpoint breakpoint : breakpoints) {
            if (!(breakpoint instanceof LineBreakpoint)) {
                continue;
            }
            LineBreakpoint lineBreakpoint = (LineBreakpoint) breakpoint;
            if (!lineBreakpoint.getURL().equals(url)) {
                continue;
            }
            if (lineBreakpoint.getLineNumber() == lineNumber) {
                return lineBreakpoint;
            }
        }
        
        return null;
    }
    
}