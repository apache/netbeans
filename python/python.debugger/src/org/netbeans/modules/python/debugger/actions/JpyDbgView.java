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
package org.netbeans.modules.python.debugger.actions;

import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;
import org.netbeans.modules.python.api.PythonOptions;
import org.netbeans.modules.python.debugger.DebuggerPythonLogger;
import org.netbeans.modules.python.debugger.PythonDebugger;
import org.netbeans.modules.python.debugger.Utils;
import org.netbeans.modules.python.debugger.backend.PluginEvent;
import org.netbeans.modules.python.debugger.breakpoints.PythonBreakpoint;
import org.netbeans.modules.python.debugger.config.NetBeansFrontend;
import org.netbeans.modules.python.debugger.spi.PythonEvent;

import org.netbeans.modules.python.debugger.gui.PythonDebugContainer;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;

import org.openide.filesystems.FileObject;

import org.openide.text.Line;

import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

public class JpyDbgView extends TopComponent
        implements PluginEventListener {

  private final static String _JPYDBG_VIEW_TITLE_ =
          PythonDebugContainer.VERSION;

  /* manage only one instance of debugger python view */
  private static JpyDbgView _debuggerView = null;
  private static PythonDebugContainer _pyContainer = null;
  private String _title = _JPYDBG_VIEW_TITLE_;
  private PythonDebugger _dbgInstance = null;
  private DebuggerPythonLogger _logger = null;
  private static boolean _isInDebugState = false;

  /**
   * Creates a new instance of JpyDbgView
   */
  public JpyDbgView() {
    NetBeansFrontend.initCheck();
    if (_pyContainer == null) {
      _pyContainer = new PythonDebugContainer(this);
    }
  }

  public static boolean isDebugging() {
    return _isInDebugState;
  }

  /**
   * Overriden to explicitely set persistence type of ProjectsTab to
   * PERSISTENCE_NEVER
   */
  @Override
  public int getPersistenceType() {
    return TopComponent.PERSISTENCE_NEVER;
  }

  /**
   * Return preferred ID
   */
  @Override
  protected String preferredID() {
    return "nbPython debug session";
  }

  /**
   * populated during python debugger instance startup
   */
  public void setDebuggerInstance(PythonDebugger pyDbg) {
    _dbgInstance = pyDbg;
  }

  /**
   * return current debugger active session
   */
  public PythonDebugger getDebuggerInstance() {
    return _dbgInstance;
  }

  /**
   * a debugging event is coming from JpyDbg kernel : handle it inside netbeans
   * debugger's structure
   */
  @Override
  public void newDebuggingEvent(PluginEvent evt) {
    if (_dbgInstance != null) {
      _dbgInstance.handleDebuggerEvent(evt);
    }
  }

  /**
   * get debugging container context back
   *
   * @return debugging context
   */
  public PythonDebugContainer getJpyDbgContext() {
    return _pyContainer;
  }

  /**
   * populate color changes from configuration panel into the debugging windows
   */
  public void applyColorChanges(PythonOptions options) {
    if (_pyContainer != null) {
      _pyContainer.parametersChanged(options);
    }
  }

  /**
   * proceed with JpyDbg launch
   */
  public void startJpyDbg(PythonEvent event) {

    // launch and wait
    _pyContainer.startDebuggerInstance(event.getSession());
    _isInDebugState = true;
  }

  class _AWT_DEBUG_OPEN_ {

    private TopComponent _candidate;
    private String _title;

    public _AWT_DEBUG_OPEN_(TopComponent candidate, String title) {
      _candidate = candidate;
      _title = title;
    }

    public void execute() {
      if (!_candidate.isOpened()) {
        _candidate.setName(_title);

        WindowManager wm = WindowManager.getDefault();
        Mode mode = wm.findMode(_candidate);

        if (mode == null) {
          mode = wm.findMode("output");
          if (mode != null) {
            mode.dockInto(_candidate);
          }
        }
        open();
      }
      requestVisible();
      requestActive();

    }
  }

  class _AWT_DEBUG_OPEN_THREAD_
          extends Thread {

    private _AWT_DEBUG_OPEN_ _opener;

    public _AWT_DEBUG_OPEN_THREAD_(TopComponent candidate, String title) {
      _opener = new _AWT_DEBUG_OPEN_(candidate, title);
    }

    @Override
    public void run() {
      _opener.execute();
    }
  }

  /**
   * request opening or activation ( if allready oppend ) of a debugging window
   *
   * @param title window's displayed title
   */
  public void openPythonDebuggingWindow(String title, boolean fromAwt) {
    if (title != null) {
      _title = title;
    }
    if (fromAwt) {
      new _AWT_DEBUG_OPEN_(this, _title).execute();
    } else {
      try {
        SwingUtilities.invokeAndWait(new _AWT_DEBUG_OPEN_THREAD_(this, _title));
      } catch (InterruptedException | InvocationTargetException e) {
        e.printStackTrace();
      }

    }
  }

  /**
   * Gets runner and debugger singletons i.e. deserialization routines,
   * otherwise you can get non-deserialized instance.
   */
  public static synchronized JpyDbgView getCurrentView() {
    if (_debuggerView == null) {
      _debuggerView = new JpyDbgView();
    }

    return _debuggerView;
  }

  /**
   * get static debugger view instance
   *
   * @return static debugger view instance
   */
  public static synchronized JpyDbgView get_debuggerView() {
    return _debuggerView;
  }

  /**
   * termination of current debugging session
   */
  public void terminateSession(PythonEvent event) {
    _pyContainer.terminateSession();

    if (_logger != null) {
      _logger.pythonFinished(event);
    }

    // finaly close debugging window
    final TopComponent wk = this;

    _dbgInstance = null;
    _logger = null;
    _isInDebugState = false;
  }

  /**
   * execute step into debug command
   */
  public void stepInto() {
    if (_pyContainer != null) {
      _pyContainer.activateStepInto();
    }
  }

  /**
   * execute step out debug command
   */
  public void stepOut() {
    if (_pyContainer != null) {
      _pyContainer.activateStepOut();
    }
  }

  /**
   * proceed with StepOver
   */
  public void stepOver() {
    if (_pyContainer != null) {
      _pyContainer.activateNext();
    }
  }

  /**
   * populate a debugger breakpoint
   *
   * @param source
   * @param line
   */
  public void setBreakPoint(PythonBreakpoint bp) {
    if (_pyContainer == null) {
      return;
    }

    Line line = bp.getLine();
    if ((line != null) && (line.getLookup() != null)) {
      FileObject fo = line.getLookup().lookup(FileObject.class);
      if (bp.isEnabled()) {
        _pyContainer.setBreakPoint(
                Utils.getPath(fo),
                line.getLineNumber() + 1  ,
                false ,  // Not a TEMP breakpoint
                bp.getCondition() ,
                bp.getHitCountFilter() ,
                bp.getHitCountFilteringStyle()
                                 );
      } else {
        _pyContainer.clearBreakPoint(
                Utils.getPath(fo),
                line.getLineNumber() + 1);
      }
    }
  }

  /**
   * clear a jpydbg breakpoint
   *
   * @param source
   * @param line
   */
  public void clearBreakPoint(PythonBreakpoint bp) {
    Line line = bp.getLine();
    FileObject fo = line.getLookup().lookup(FileObject.class);
    _pyContainer.clearBreakPoint(
            fo.getPath(),
            line.getLineNumber() + 1);
  }

  /**
   * on debugger starting event just populate all currently set breakpoints to
   * debugger's server side
   */
  public void populateBreakPoints() {
    Breakpoint[] breakpoints =
            DebuggerManager.getDebuggerManager().getBreakpoints();
    int i, k = breakpoints.length;
    for (i = 0; i < k; i++) {
      if (breakpoints[i] instanceof PythonBreakpoint) {
        PythonBreakpoint bp = (PythonBreakpoint) breakpoints[i];
        setBreakPoint(bp);
      }
    }
  }

  /**
   * go ahead until next breakpoint or script termination
   */
  public void doContinue( boolean toCursor ) {
    if (_pyContainer != null) {
      _pyContainer.activateContinue(toCursor);
    }
  }

  /**
   * Set debugger tasking parent instance
   *
   * @param logger tasking instance
   */
  public void set_logger(DebuggerPythonLogger logger) {
    _logger = logger;
  }

  /**
   * provide a way to write logging info in PythonDebugContainer console as
   * Header type messages
   */
  public void writeLog(String log) {
    _pyContainer.writeLog(log);
  }
}
