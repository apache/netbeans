/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
