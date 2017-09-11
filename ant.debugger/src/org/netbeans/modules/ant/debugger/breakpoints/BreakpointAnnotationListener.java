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

package org.netbeans.modules.ant.debugger.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;

import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.modules.ant.debugger.DebuggerBreakpointAnnotation;


/**
 * Listens on {@org.netbeans.api.debugger.DebuggerManager} on
 * {@link org.netbeans.api.debugger.DebuggerManager#PROP_BREAKPOINTS}
 * property and annotates JPDA Debugger line breakpoints in NetBeans editor.
 *
 * @author Jan Jancura
 */
public class BreakpointAnnotationListener extends DebuggerManagerAdapter 
implements PropertyChangeListener {
    
    private Map breakpointToAnnotation = new HashMap ();
    
 
    @Override
    public String[] getProperties () {
        return new String[] {DebuggerManager.PROP_BREAKPOINTS};
    }

    /**
    * Called when some breakpoint is added.
    *
    * @param b breakpoint
    */
    @Override
    public void breakpointAdded (Breakpoint b) {
        if (! (b instanceof AntBreakpoint)) return;
        addAnnotation ((AntBreakpoint) b);
    }

    /**
    * Called when some breakpoint is removed.
    *
    * @param breakpoint
    */
    @Override
    public void breakpointRemoved (Breakpoint b) {
        if (! (b instanceof AntBreakpoint)) return;
        removeAnnotation (b);
    }

    /**
     * This method gets called when a bound property is changed.
     * @param evt A PropertyChangeEvent object describing the event source 
     *   	and the property that has changed.
     */

    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        if (evt.getPropertyName () != Breakpoint.PROP_ENABLED) return;
        removeAnnotation ((Breakpoint) evt.getSource ());
        addAnnotation ((AntBreakpoint) evt.getSource ());
    }
    
    private void addAnnotation (AntBreakpoint b) {
        breakpointToAnnotation.put (
            b,
            new DebuggerBreakpointAnnotation (
                b.isEnabled () ? 
                    DebuggerBreakpointAnnotation.BREAKPOINT_ANNOTATION_TYPE :
                    DebuggerBreakpointAnnotation.DISABLED_BREAKPOINT_ANNOTATION_TYPE, 
                b
            )
        );
        b.addPropertyChangeListener (
            Breakpoint.PROP_ENABLED, 
            this
        );
    }
    
    private void removeAnnotation (Breakpoint b) {
        DebuggerBreakpointAnnotation annotation = (DebuggerBreakpointAnnotation) 
            breakpointToAnnotation.remove (b);
        if (annotation == null) return;
        annotation.detach ();
        b.removePropertyChangeListener (
            Breakpoint.PROP_ENABLED, 
            this
        );
    }
}
