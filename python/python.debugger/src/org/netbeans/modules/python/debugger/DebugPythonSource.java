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
package org.netbeans.modules.python.debugger;

import java.io.IOException;
import org.netbeans.modules.python.debugger.actions.JpyDbgView;
import org.netbeans.modules.python.debugger.config.NetBeansFrontend;
import org.netbeans.modules.python.debugger.spi.PythonSession;
import org.netbeans.modules.python.debugger.spi.PythonDebuggerTargetExecutor;
import org.openide.execution.ExecutorTask;
import org.openide.util.RequestProcessor;

/**
 * Starting Python debug main classes ( debug scripts , remote debug )
 */
public class DebugPythonSource
        implements Runnable {

  public final static String EMPTY = "";
  private JpyDbgView _dbgView = null;
  private PythonSession _curSession = null;
  private boolean _isRemote = false;
  private Debuggee _debuggee;

  /* run in non debug mode */
  /** Creates a new instance of DebugPythonSource */
  public DebugPythonSource(
          Debuggee debuggee,
          boolean remote) {
    _debuggee = debuggee;
    _isRemote = remote;
    //if (!_isRemote)
    //  _argsCombo = modConf ;
    init();
  }

  private void init() {
    // first check for correct initializations
    // of IDE frontend module
    NetBeansFrontend.initCheck();

    _dbgView = JpyDbgView.getCurrentView();

  }

  public void startDebugging() {
    String title = "Debugging " + _debuggee.getFileObject().getNameExt();
    _dbgView.openPythonDebuggingWindow(title, false);
    _debuggee.setDebugView(_dbgView);
    _curSession = new PythonSession(_debuggee,
            _isRemote);

    RequestProcessor.getDefault().post(this);
  }

  /**
   * Thread dealing with Python Debug or Run task 
   */
  @Override
  public void run() {
    try {
      PythonDebuggerTargetExecutor.Env env =
              new PythonDebuggerTargetExecutor.Env();
      PythonDebuggerTargetExecutor executor =
              PythonDebuggerTargetExecutor.createTargetExecutor(env);
      _debuggee.setSession(_curSession);
      ExecutorTask task = executor.execute(_debuggee);
    } catch (IOException ioe) {
      PythonDebuggerModule.err.notify(ioe);
    }
  }
}
