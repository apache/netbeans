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

package org.netbeans.modules.groovy.support.debug;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Set;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.spi.debugger.ActionsProvider.Registration;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.filesystems.FileObject;

/** 
 * Toggle Groovy Breakpoint action provider.
 *
 * @author Martin Grebac
 * @author Martin Adamek
 */
@Registration(actions={"toggleBreakpoint"}, activateForMIMETypes={"text/x-groovy"})
public class GroovyToggleBreakpointActionProvider extends ActionsProviderSupport implements PropertyChangeListener {
    
    private JPDADebugger debugger;
    
    public GroovyToggleBreakpointActionProvider () {
        Context.addPropertyChangeListener (this);
        setEnabled (ActionsManager.ACTION_TOGGLE_BREAKPOINT, false);
    }
    
    public GroovyToggleBreakpointActionProvider (ContextProvider contextProvider) {
        debugger = contextProvider.lookupFirst(null, JPDADebugger.class);
        debugger.addPropertyChangeListener (JPDADebugger.PROP_STATE, this);
        Context.addPropertyChangeListener (this);
        setEnabled (ActionsManager.ACTION_TOGGLE_BREAKPOINT, false);
    }
    
    private void destroy () {
        debugger.removePropertyChangeListener (JPDADebugger.PROP_STATE, this);
        Context.removePropertyChangeListener (this);
    }
    
    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        FileObject fo = Context.getCurrentFile();
        boolean isGroovyFile = fo != null && 
                "text/x-groovy".equals(fo.getMIMEType()); // NOI18N [TODO]
        
        setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT, isGroovyFile);
        if ( debugger != null && 
             debugger.getState () == JPDADebugger.STATE_DISCONNECTED
        ) 
            destroy ();
    }
    
    @Override
    public Set getActions () {
        return Collections.singleton (ActionsManager.ACTION_TOGGLE_BREAKPOINT);
    }
    
    @Override
    public void doAction (Object action) {
        DebuggerManager debugManager = DebuggerManager.getDebuggerManager ();
        
        // 1) get source name & line number
        int lineNumber = Context.getCurrentLineNumber ();
        String url = Context.getCurrentURL ();
        if (url == null) return;
                
        // 2) find and remove existing line breakpoint
        for (Breakpoint breakpoint : debugManager.getBreakpoints()) {
            if (breakpoint instanceof LineBreakpoint) {
                
                LineBreakpoint lineBreakpoint = ((LineBreakpoint) breakpoint);
                if (lineNumber == lineBreakpoint.getLineNumber() && url.equals(lineBreakpoint.getURL())) {
                    debugManager.removeBreakpoint(breakpoint);
                    return;
                }
            }
        }

        // 3) Add new groovy line breakpoint
        debugManager.addBreakpoint(GroovyLineBreakpointFactory.create(url, lineNumber));
    }
}
