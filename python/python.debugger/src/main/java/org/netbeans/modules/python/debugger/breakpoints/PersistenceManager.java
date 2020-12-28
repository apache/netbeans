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

import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.Breakpoint;
import java.util.ArrayList;
import java.beans.PropertyChangeEvent;
import java.util.Vector;

/**
 *
 * @author jean-yves Mengant
 */
public class PersistenceManager
        implements LazyDebuggerManagerListener {

  private final static String _PYTHON_ = "python";
  private final static String _DEBUGGER_ = "debugger";

  /** Creates a new instance of PersistenceManager */
  public PersistenceManager() {
  }

  @Override
  public String[] getProperties() {
    return new String[]{
              DebuggerManager.PROP_BREAKPOINTS_INIT,
              DebuggerManager.PROP_BREAKPOINTS,};
  }

  @Override
  public void breakpointRemoved(Breakpoint breakpoint) {
    Properties p = Properties.getDefault().getProperties(_DEBUGGER_).
            getProperties(DebuggerManager.PROP_BREAKPOINTS);
    p.setArray(
            _PYTHON_,
            getBreakpoints());
    breakpoint.removePropertyChangeListener(this);
  }

  @Override
  public void sessionAdded(Session session) {
  }

  @Override
  public void sessionRemoved(Session session) {
  }

  @Override
  public void engineAdded(DebuggerEngine engine) {
  }

  @Override
  public void engineRemoved(DebuggerEngine engine) {
  }

  @Override
  public void watchAdded(Watch watch) {
  }

  @Override
  public void watchRemoved(Watch watch) {
  }

  @Override
  public void initWatches() {
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (evt.getSource() instanceof PythonBreakpoint) {
      Properties.getDefault().getProperties(_DEBUGGER_).
              getProperties(DebuggerManager.PROP_BREAKPOINTS).setArray(
              _PYTHON_,
              getBreakpoints());
    }
  }

  @Override
  public Breakpoint[] initBreakpoints() {
    Properties p = Properties.getDefault().getProperties(_DEBUGGER_).
            getProperties(DebuggerManager.PROP_BREAKPOINTS);
    Breakpoint[] wkArray = (Breakpoint[]) p.getArray(
            _PYTHON_,
            new Breakpoint[0]);
    // chase for null file or line breakpoints and remove them
    // (Filezilla 150543
    Vector wk = new Vector();
    for (Breakpoint brkpoint : wkArray) {
      PythonBreakpoint cur = (PythonBreakpoint) brkpoint;
      if (cur.getLine() != null) {
        cur.addPropertyChangeListener(this);
        wk.add(cur);
      }
    }

    return (Breakpoint[]) wk.toArray(new Breakpoint[0]);
  }

  @Override
  public void breakpointAdded(Breakpoint breakpoint) {
    if (breakpoint instanceof PythonBreakpoint) {

      Properties p = Properties.getDefault().getProperties(_DEBUGGER_).
              getProperties(DebuggerManager.PROP_BREAKPOINTS);
      p.setArray(
              _PYTHON_,
              getBreakpoints());
      breakpoint.addPropertyChangeListener(this);
    }
  }

  private static Breakpoint[] getBreakpoints() {
    Breakpoint[] bs = DebuggerManager.getDebuggerManager().getBreakpoints();
    int i, k = bs.length;
    ArrayList bb = new ArrayList();
    for (i = 0; i < k; i++) // Don't store hidden breakpoints
    {
      if (bs[i] instanceof PythonBreakpoint) {
        bb.add(bs[i]);
      }
    }
    bs = new Breakpoint[bb.size()];
    return (Breakpoint[]) bb.toArray(bs);
  }
}
