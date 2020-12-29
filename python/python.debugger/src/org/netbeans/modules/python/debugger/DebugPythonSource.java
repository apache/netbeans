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
