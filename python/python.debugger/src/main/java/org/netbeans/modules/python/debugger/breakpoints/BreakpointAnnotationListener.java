/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.python.debugger.breakpoints;

import org.netbeans.api.debugger.DebuggerManagerAdapter;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.HashMap;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Breakpoint;
import java.beans.PropertyChangeEvent;
import org.netbeans.modules.python.debugger.DebuggerBreakpointAnnotation;
import org.openide.text.AnnotationProvider;
import org.openide.text.Line.Set;
import org.openide.util.Lookup;

/**
 * Netbeans breakpoint semantics
 * Listens on {@org.netbeans.api.debugger.DebuggerManager} on
 * {@link org.netbeans.api.debugger.DebuggerManager#PROP_BREAKPOINTS} 
 * property and annotates JPDA Debugger line breakpoints in NetBeans editor.
 * @author jean-yves Mengant
 */

@org.openide.util.lookup.ServiceProvider(service=org.openide.text.AnnotationProvider.class)
public class BreakpointAnnotationListener
        extends DebuggerManagerAdapter
        implements PropertyChangeListener , AnnotationProvider {

  private Map _breakpointToAnnotation = new HashMap();

  @Override
  public String[] getProperties() {
    return new String[]{DebuggerManager.PROP_BREAKPOINTS};
  }

  /**
   * Called when some breakpoint is added.
   *
   * @param b breakpoint
   */
  @Override
  public void breakpointAdded(Breakpoint b) {
    if (!(b instanceof PythonBreakpoint)) {
      return;
    }
    addAnnotation(b);
  }

  /**
   * Called when some breakpoint is removed.
   *
   * @param breakpoint
   */
  @Override
  public void breakpointRemoved(Breakpoint b) {
    if (!(b instanceof PythonBreakpoint)) {
      return;
    }
    removeAnnotation(b);
  }

  /**
   * This method gets called when a bound property is changed.
   * @param evt A PropertyChangeEvent object describing the event source 
   *   	and the property that has changed.
   */
  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (evt.getPropertyName() != Breakpoint.PROP_ENABLED) {
      return;
    }
    removeAnnotation((Breakpoint) evt.getSource());
    addAnnotation((Breakpoint) evt.getSource());
  }

  private void addAnnotation(Breakpoint b) {
    _breakpointToAnnotation.put(
            b,
            new DebuggerBreakpointAnnotation(
              b.isEnabled() ? DebuggerBreakpointAnnotation.BREAKPOINT_ANNOTATION_TYPE :
                              DebuggerBreakpointAnnotation.DISABLED_BREAKPOINT_ANNOTATION_TYPE,
              ( (PythonBreakpoint) b).getLine() ,
              b ) );
    b.addPropertyChangeListener(
            Breakpoint.PROP_ENABLED,
            this);
  }


  private void removeAnnotation(Breakpoint b) {
    DebuggerBreakpointAnnotation annotation = (DebuggerBreakpointAnnotation) _breakpointToAnnotation.remove(b);
    if (annotation == null) {
      return;
    }
    annotation.detach();
    b.removePropertyChangeListener(
            Breakpoint.PROP_ENABLED,
            this);
  }

  @Override
    public void annotate(Set set, Lookup context) {
        DebuggerManager.getDebuggerManager().getBreakpoints();
    }

}
