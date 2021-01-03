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
package org.netbeans.modules.python.debugger.spi;

import java.io.File;
import org.netbeans.modules.python.debugger.Debuggee;
import org.netbeans.modules.python.debugger.actions.JpyDbgView;

/**
 * A Python debugging session instance
 */
public class PythonSession {

  private File _source;
  private JpyDbgView _dbgView;
  private String _args = null;
  private String _scriptArgs = null;
  private boolean _isRemote = false;
  private boolean _isJython = false;
  private Debuggee _debuggee = null;

  /** Creates a new instance of PythonSession */
  public PythonSession(Debuggee debuggee,
          boolean remote) {
    _debuggee = debuggee;
    _source = _debuggee.getFile();
    _dbgView = _debuggee.getDebugView();
    _args = _debuggee.getCommandArgs();
    _scriptArgs = _debuggee.getScriptArgs();
    _isRemote = remote;
    _isJython = _debuggee.is_jython();
  }

  /**
   * Get a display name used for the session as a whole.
   * @return a user-presentable display name appropriate for session-scope messaging
   */
  public String getDisplayName() {
    return _source.toString();
  }

  /**
   * Get the Python script originally invoked.
   * Note that due to cross modules calls some events may come from other scripts.
   * @return the Python script which was run to start with
   */
  public File getOriginatingScript() {
    return _source;
  }

  public String get_args() {
    return _args;
  }

  public String get_scriptArgs() {
    return _scriptArgs;
  }

  public JpyDbgView get_dbgView() {
    return _dbgView;
  }

  public boolean isRemote() {
    return _isRemote;
  }

  public boolean isDebug() {
    return _isRemote;
  }

  public boolean isJython() {
    return _isJython;
  }

  public Debuggee getDebuggee() {
    return _debuggee;
  }
}
