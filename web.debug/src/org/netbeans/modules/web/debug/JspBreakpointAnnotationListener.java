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
package org.netbeans.modules.web.debug;

import java.beans.*;
import java.util.HashMap;
import java.util.Iterator;

import org.netbeans.api.debugger.*;
import org.netbeans.api.debugger.jpda.*;

import org.netbeans.modules.web.debug.breakpoints.*;

/**
 * Listens on {@org.netbeans.api.debugger.DebuggerManager} on
 * {@link org.netbeans.api.debugger.DebuggerManager#PROP_BREAKPOINTS}
 * property and annotates
 * JSP breakpoints in NetBeans editor.
 *
 * @author Martin Grebac
 */
public class JspBreakpointAnnotationListener extends DebuggerManagerAdapter {
    
    private HashMap breakpointToAnnotation = new HashMap ();
    private boolean listen = true;
    
    public String[] getProperties () {
        return new String[] {DebuggerManager.PROP_BREAKPOINTS};
    }
    
    /**
     * Listens on breakpoint.
     */
    public void propertyChange (PropertyChangeEvent e) {
        String propertyName = e.getPropertyName ();
        if (propertyName == null) return;
        if (!listen) return;
        if ( (!propertyName.equals (JspLineBreakpoint.PROP_CONDITION)) &&
             (!propertyName.equals (JspLineBreakpoint.PROP_URL)) &&
             (!propertyName.equals (JspLineBreakpoint.PROP_LINE_NUMBER)) &&
             (!propertyName.equals (JspLineBreakpoint.PROP_ENABLED)) &&
             (!propertyName.equals (JspLineBreakpoint.PROP_VALIDITY))
        ) return;
        JspLineBreakpoint b = (JspLineBreakpoint) e.getSource ();
        annotate (b);
    }

    /**
    * Called when some breakpoint is added.
    *
    * @param b breakpoint
    */
    public void breakpointAdded (Breakpoint b) {
        if (b instanceof JspLineBreakpoint) {
            ((JspLineBreakpoint) b).addPropertyChangeListener (this);
            annotate ((JspLineBreakpoint) b);
        }
    }

    /**
    * Called when some breakpoint is removed.
    *
    * @param breakpoint
    */
    public void breakpointRemoved (Breakpoint b) {
        if (b instanceof JspLineBreakpoint) {
            ((JspLineBreakpoint) b).removePropertyChangeListener (this);
            removeAnnotation ((JspLineBreakpoint) b);
        }
    }

    public JspLineBreakpoint findBreakpoint (String url, int lineNumber) {
        Iterator i = breakpointToAnnotation.keySet ().iterator ();
        while (i.hasNext ()) {
            JspLineBreakpoint lb = (JspLineBreakpoint) i.next ();
            if (!lb.getURL ().equals (url)) continue;
            Object annotation = breakpointToAnnotation.get (lb);
            int ln = Context.getLineNumber (annotation, null);
            if (ln == lineNumber) return lb;
        }
        return null;
    }
    
    // helper methods ..........................................................
    
    private void annotate (JspLineBreakpoint b) {
        // remove old annotation
        Object annotation = breakpointToAnnotation.get (b);
        if (annotation != null)
            Context.removeAnnotation (annotation);
        if (b.isHidden ()) return;
        
        // add new one
        annotation = Context.annotate (b);
        if (annotation == null)
            return;
        
        breakpointToAnnotation.put (b, annotation);
        
        DebuggerEngine de = DebuggerManager.getDebuggerManager ().
            getCurrentEngine ();
        Object timeStamp = null;
        if (de != null)
            timeStamp = de.lookupFirst (null, JPDADebugger.class);
        update (b, timeStamp);        
    }

    public void updateJspLineBreakpoints () {
        Iterator it = breakpointToAnnotation.keySet ().iterator (); 
        while (it.hasNext ()) {
            JspLineBreakpoint lb = (JspLineBreakpoint) it.next ();
            update (lb, null);
        }
    }
    
    private void update (JspLineBreakpoint b, Object timeStamp) {
        Object annotation = breakpointToAnnotation.get (b);
        if (annotation == null) 
            return;
        int ln = Context.getLineNumber (annotation, timeStamp);
        listen = false;
        b.setLineNumber (ln);
        listen = true;
    }
    
    private void removeAnnotation(JspLineBreakpoint b) {
        Object annotation = breakpointToAnnotation.remove (b);
        if (annotation != null)
            Context.removeAnnotation (annotation);
    }
}
