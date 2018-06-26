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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.php.dbgp.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.modules.php.dbgp.annotations.BrkpntAnnotation;
import org.netbeans.modules.php.dbgp.annotations.DisabledBrkpntAnnotation;
import org.openide.text.Annotation;
import org.openide.text.AnnotationProvider;
import org.openide.text.Line;
import org.openide.text.Line.Set;
import org.openide.util.Lookup;

/**
 * Listens on {@org.netbeans.api.debugger.DebuggerManager} on
 * {@link org.netbeans.api.debugger.DebuggerManager#PROP_BREAKPOINTS} property
 * and annotates Debugger line breakpoints in NetBeans editor.
 *
 * @author ads
 */
@org.openide.util.lookup.ServiceProvider(service = org.openide.text.AnnotationProvider.class)
public class BreakpointAnnotationListener extends DebuggerManagerAdapter implements PropertyChangeListener, AnnotationProvider {
    private Map<Breakpoint, Annotation> myAnnotations = new HashMap<>();

    @Override
    public String[] getProperties() {
        return new String[]{DebuggerManager.PROP_BREAKPOINTS};
    }

    @Override
    public void breakpointAdded(Breakpoint breakpoint) {
        if (!(breakpoint instanceof LineBreakpoint)) {
            return;
        }
        addAnnotation(breakpoint);
    }

    @Override
    public void breakpointRemoved(Breakpoint breakpoint) {
        if (!(breakpoint instanceof LineBreakpoint)) {
            return;
        }
        removeAnnotation(breakpoint);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!Breakpoint.PROP_ENABLED.equals(evt.getPropertyName())) {
            return;
        }
        removeAnnotation((Breakpoint) evt.getSource());
        addAnnotation((Breakpoint) evt.getSource());
    }

    private void addAnnotation(Breakpoint breakpoint) {
        LineBreakpoint lineBreakpoint = (LineBreakpoint) breakpoint;
        Line line = lineBreakpoint.getLine();
        Annotation annotation = breakpoint.isEnabled()
                ? new BrkpntAnnotation(line, lineBreakpoint)
                : new DisabledBrkpntAnnotation(line, lineBreakpoint);
        myAnnotations.put(breakpoint, annotation);
        breakpoint.addPropertyChangeListener(Breakpoint.PROP_ENABLED, this);
    }

    private void removeAnnotation(Breakpoint breakpoint) {
        Annotation annotation = myAnnotations.remove(breakpoint);
        if (annotation == null) {
            return;
        }
        annotation.detach();
        breakpoint.removePropertyChangeListener(Breakpoint.PROP_ENABLED, this);
    }

    @Override
    public void annotate(Set set, Lookup context) {
        DebuggerManager.getDebuggerManager().getBreakpoints();
    }

}
