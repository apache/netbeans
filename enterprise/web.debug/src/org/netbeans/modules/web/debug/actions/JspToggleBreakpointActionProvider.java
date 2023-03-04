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

package org.netbeans.modules.web.debug.actions;

import java.util.*;
import java.beans.*;

import org.netbeans.api.debugger.*;
import org.netbeans.api.debugger.jpda.*;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.spi.debugger.*;

import org.netbeans.modules.web.debug.Context;
import org.netbeans.modules.web.debug.JspBreakpointAnnotationListener;
import org.netbeans.modules.web.debug.util.Utils;
import org.netbeans.modules.web.debug.breakpoints.JspLineBreakpoint;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/** 
 * Toggle JSP Breakpoint action provider.
 *
 * @author Martin Grebac
 */
@ActionsProvider.Registration(actions={"toggleBreakpoint"}, activateForMIMETypes={"text/x-jsp", "text/x-tag"})
public class JspToggleBreakpointActionProvider extends ActionsProviderSupport implements PropertyChangeListener {
    
    
    private JPDADebugger debugger;

    
    public JspToggleBreakpointActionProvider () {
        Context.addPropertyChangeListener (this);
    }
    
    public JspToggleBreakpointActionProvider (ContextProvider contextProvider) {
        debugger = (JPDADebugger) contextProvider.lookupFirst 
                (null, JPDADebugger.class);
        debugger.addPropertyChangeListener (debugger.PROP_STATE, this);
        Context.addPropertyChangeListener (this);
    }
    
    private void destroy () {
        if (debugger != null) {
            debugger.removePropertyChangeListener (debugger.PROP_STATE, this);
        }
        Context.removePropertyChangeListener (this);
    }
    
    public void propertyChange (PropertyChangeEvent evt) {
        //#67910 - setting of a bp allowed only in JSP contained in some web module
        FileObject fo = Context.getCurrentFile();
        setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT, isJSP(fo));
        if ( debugger != null && 
             debugger.getState () == debugger.STATE_DISCONNECTED
        ) 
            destroy ();
    }
    
    private boolean isJSP(FileObject fo) {
        WebModule owner = null;
        if (fo != null) {
            owner = WebModule.getWebModule(fo);
        }
        
        boolean isJsp = Utils.isJsp(fo) || Utils.isTag(fo);
        
        String webRoot = null;
        if (owner != null && owner.getDocumentBase() != null) {
            webRoot = FileUtil.getRelativePath(owner.getDocumentBase(), fo);
        }

        //#issue 65969 fix:
        //we allow bp setting only if the file is JSP or TAG file
        //TODO it should be solved by adding new API into j2eeserver which should announce whether the target server
        //supports JSP debugging or not
        return owner != null && webRoot != null && isJsp;
    }
    
    public Set getActions () {
        return Collections.singleton (ActionsManager.ACTION_TOGGLE_BREAKPOINT);
    }
    
    public void doAction (Object action) {
        DebuggerManager d = DebuggerManager.getDebuggerManager ();
        
        // 1) get source name & line number
        int ln = Context.getCurrentLineNumber ();
        FileObject fo = Context.getCurrentFile();
        if (!isJSP(fo)) {
            return ;
        }
        String url = fo.toURL().toString();
                
        // 2) find and remove existing line breakpoint
        JspLineBreakpoint lb = getJspBreakpointAnnotationListener().findBreakpoint(url, ln);        
        if (lb != null) {
            d.removeBreakpoint(lb);
            return;
        }
        lb = JspLineBreakpoint.create(url, ln);
        d.addBreakpoint(lb);
    }

    private JspBreakpointAnnotationListener jspBreakpointAnnotationListener;
    private JspBreakpointAnnotationListener getJspBreakpointAnnotationListener () {
        if (jspBreakpointAnnotationListener == null)
            jspBreakpointAnnotationListener = (JspBreakpointAnnotationListener) 
                DebuggerManager.getDebuggerManager ().lookupFirst 
                (null, JspBreakpointAnnotationListener.class);
        return jspBreakpointAnnotationListener;
    }
}
