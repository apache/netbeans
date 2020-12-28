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
 */package org.netbeans.modules.python.debugger.backend;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import org.netbeans.modules.python.debugger.utils.ExecTerminationEvent;
import org.netbeans.modules.python.debugger.utils.ExecTerminationListener;
import org.netbeans.modules.python.debugger.utils.ProcessLauncher;
import org.netbeans.modules.python.debugger.utils.UtilsError;

/**
 * @author jean-yves Mengant
 *
 * implement a local python interpretor launcher utility
 *
 */
public class PythonInterpretor
        extends Thread
        implements ExecTerminationListener {

  private ProcessLauncher _launcher = new ProcessLauncher();
  private Vector _command = null;
  private PythonDebugEventListener _listener = null;

  public synchronized void addPythonDebugEventListener(PythonDebugEventListener l) {
    _listener = l;
  }

  public synchronized void removePythonDebugEventListener() {
    _listener = null;
  }

  private void populateInterpretorEvent(int type, String msg) {
    if (_listener != null) {
      PythonDebugEvent evt = new PythonDebugEvent(type, msg);
      _listener.launcherMessage(evt);
    }
  }

  public void setEnv(String name, String value) {
    _launcher.setEnv(name, value);
  }

  public Process getProcess() {
    return _launcher.getProcess();
  }

  public PythonInterpretor(String pgm, Vector args) {
    // For both CPYTHON an JYTHON we only need the shell execution path location
    _command = args;
    _command.insertElementAt(pgm, 0);
  }

  class _DBG_STREAM_READER_
          extends Thread {

    private int _type;
    private InputStream _istream;
    private StringBuffer _resultBuffer;

    public _DBG_STREAM_READER_(int type, InputStream istream) {
      _type = type;
      _istream = istream;
      _resultBuffer = new StringBuffer();
    }

    @Override
    public void run() {
      int c;

      try {
        while ((_istream != null) && (c = _istream.read()) != -1) {
          if (c == '\n') {
            populateInterpretorEvent(_type, _resultBuffer.toString());
            _resultBuffer = new StringBuffer();
          } else {
            _resultBuffer.append((char) c);
          }

        }
        if (_istream != null) {
          _istream.close();
        }
      } catch (IOException e) {
        populateInterpretorEvent(PythonDebugEvent.LAUNCHER_ERR, e.getMessage());
      }
    }
  }

  /**
   * Daemon ending
   */
  @Override
  public void processHasEnded(ExecTerminationEvent evt) {
    int code = evt.get_code();
    String retCode = Integer.toString(code);
    populateInterpretorEvent(PythonDebugEvent.LAUNCHER_ENDING, retCode);
  }

  public void doTheJob() {
    try {
      _launcher.setCommand(_command);
      _launcher.setExecTerminationListener(this);
      // run debugger through ProcessBuilder thread
      _launcher.go();
      _DBG_STREAM_READER_ stdoutReader = new _DBG_STREAM_READER_(PythonDebugEvent.LAUNCHER_MSG, _launcher.getStdout());
      _DBG_STREAM_READER_ stderrReader = new _DBG_STREAM_READER_(PythonDebugEvent.LAUNCHER_MSG, _launcher.getStderr());
      stdoutReader.start();
      stderrReader.start();
      _launcher.waitForCompletion();
    } catch (UtilsError e) {
      populateInterpretorEvent(PythonDebugEvent.LAUNCHER_ERR, e.getMessage());
    }
  }

  @Override
  public void run() {
    doTheJob();
  }

  public static void main(String[] args) {
  }
}
