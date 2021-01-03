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
