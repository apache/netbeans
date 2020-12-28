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
package org.netbeans.modules.python.debugger.spi;

import org.netbeans.modules.python.debugger.PythonSourceDebuggee;
import org.openide.windows.InputOutput;
import org.openide.execution.ExecutorTask;
import org.openide.windows.IOProvider;
import java.io.IOException;
import org.openide.execution.ExecutionEngine;
import org.openide.util.RequestProcessor;
import java.io.OutputStream;
import org.netbeans.modules.python.debugger.actions.JpyDbgView;
import org.netbeans.modules.python.debugger.DebuggerPythonLogger;
import org.openide.util.Task;
import org.openide.util.TaskListener;

/**
 *
 * @author jean-yves Mengant
 */
public class TargetExecutor
        implements Runnable {

  private PythonSourceDebuggee _pcookie;
  private InputOutput _io = null;
  /** used for the tab etc. */
  private String _displayName;
  private boolean _ok = false;
  private OutputStream _outputStream;
  private JpyDbgView _dbgView;

  /** targets may be null to indicate default target */
  public TargetExecutor(PythonSourceDebuggee pcookie) {
    _pcookie = pcookie;
    _dbgView = pcookie.getDebugView();
  }

  private static class _WRAPPERRUNNABLE_ implements Runnable {

    private final ExecutorTask _task;

    public _WRAPPERRUNNABLE_(ExecutorTask task) {
      _task = task;
    }

    @Override
    public void run() {
      _task.waitFinished();
    }
  }

  private class _TASKTERMINATIONHANDLER_
          implements TaskListener {

    @Override
    public void taskFinished(Task task) {
      System.out.println("Entering task termination");
      // proceed with debug termination
      // TODO analyze debug termination actions
      if (_dbgView != null) {
        _dbgView.terminateSession(new PythonEvent(PythonEvent.STOP_SESSION, _pcookie.getSession()));
      }
    }
  }

  private class _WRAPPEREXECUTORTASK_ extends ExecutorTask {

    private ExecutorTask _task;
    private InputOutput _io;

    public _WRAPPEREXECUTORTASK_(ExecutorTask task, InputOutput io) {
      super(new _WRAPPERRUNNABLE_(task));
      _task = task;
      _io = io;
    }

    @Override
    public void stop() {
      _task.stop();
    }

    @Override
    public int result() {
      return _task.result() + (_ok ? 0 : 1);
    }

    @Override
    public InputOutput getInputOutput() {
      return _io;
    }
  }

  public ExecutorTask execute() throws IOException {
    String fileName;
    // System.out.println("entering targetexecutor execute") ;
    if (_pcookie.getFileObject() != null) {
      fileName = _pcookie.getFileObject().getNameExt();
    } else {
      fileName = _pcookie.getFile().getName();
    }
    _displayName = fileName + "(Debugging)";

    if (_io == null) {
      if (_dbgView == null) {
        _io = IOProvider.getDefault().getIO(_displayName, true);
      }
    }

    final ExecutorTask task = ExecutionEngine.getDefault().execute(_displayName, this, InputOutput.NULL);
    // capture termination request event
    task.addTaskListener(new _TASKTERMINATIONHANDLER_());
    _WRAPPEREXECUTORTASK_ wrapper = new _WRAPPEREXECUTORTASK_(task, _io);

    RequestProcessor.getDefault().post(wrapper);
    // System.out.println("leaving targetexecutor execute") ;
    return wrapper;

  }

  public ExecutorTask execute(OutputStream outputStream)
          throws IOException {
    _outputStream = outputStream;
    ExecutorTask task = ExecutionEngine.getDefault().execute(
            "LABEL_execution_name", this, InputOutput.NULL);
    return new _WRAPPEREXECUTORTASK_(task, null);
  }

  @Override
  synchronized public void run() {
    System.out.println("entering targetexecutor THREAD");

    PythonSession session = _pcookie.getSession();

    DebuggerPythonLogger dbgTask = DebuggerPythonLogger.getDefault();
    dbgTask.debugFile(session.getOriginatingScript());

    // Start debugging session
    dbgTask.taskStarted(
            new PythonEvent(PythonEvent.START_SESSION,
            session));
    _dbgView.set_logger(dbgTask);

    System.out.println("leaving targetexecutor THREAD");
  }
}
