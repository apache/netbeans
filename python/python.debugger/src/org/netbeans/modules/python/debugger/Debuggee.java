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

import java.io.File;
import java.util.Hashtable;
import org.netbeans.modules.python.api.PythonPlatform;
import org.netbeans.modules.python.debugger.actions.JpyDbgView;
import org.netbeans.modules.python.debugger.spi.PythonSession;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.RequestProcessor;

/**
 * Define candidate for debug session
 */
public class Debuggee
        implements PythonSourceDebuggee {

  private final static Hashtable<FileObject, Debuggee> _debuggees = new Hashtable<>();
  private final static String _PYTHON_ = "python";

  // execution commands
  private PythonPlatform _platform;
  private String _command;
  private String _workingDirectory;
  private String _commandArgs;
  private String _path;
  private String _javaPath;
  private String _scriptName;
  private String _scriptArgs;
  private String _displayName;
  private boolean _jython = false;
  private PythonSession _pythonSession = null;
  private JpyDbgView _view = null;
  private RequestProcessor _rp;
  private DataObject _pyo;

  public Debuggee(DataObject pyo) {
    _pyo = pyo;
    _rp = new RequestProcessor("Debuggee[" + getFileObject() + "]");
  }

  public synchronized String getCommand() {
    return _command;
  }

  public synchronized void setPlatform(PythonPlatform platform) {
    _platform = platform;
    if (_platform.getInterpreterCommand().endsWith(_PYTHON_)) {
      _jython = false;
    } else {
      _jython = true;
    }
  }

  public synchronized boolean is_jython() {
    return _jython;
  }

  public synchronized String getCommandArgs() {
    return _commandArgs;
  }

  public synchronized String getPath() {
    return _path;
  }

  public synchronized void setPath(String path) {
    _path = path;
  }

  public synchronized String getJavaPath() {
    return _javaPath;
  }

  public synchronized void setJavaPath(String path) {
    _javaPath = path;
  }

  public synchronized String getScript() {
    return _scriptName;
  }

  public synchronized void setScript(String script) {
    _scriptName = script;
  }

  public synchronized String getScriptArgs() {
    return _scriptArgs;
  }

  public synchronized void setScriptArgs(String scriptArgs) {
    _scriptArgs = scriptArgs;
  }

  public synchronized String getWorkingDirectory() {
    return _workingDirectory;
  }

  public synchronized PythonPlatform getPlatform() {
    return _platform;
  }

  public synchronized void setWorkingDirectory(String workingDirectory) {
    _workingDirectory = workingDirectory;
  }

  public synchronized String getDisplayName() {
    return _displayName;
  }

  public synchronized void setDisplayName(String displayName) {
    this._displayName = displayName;
  }

  @Override
  public File getFile() {
    FileObject fo = getFileObject();
    if (fo != null) {
      return FileUtil.toFile(fo);
    } else {
      return null;
    }
  }

  @Override
  public FileObject getFileObject() {
    if (_pyo == null) {
      return null;
    }
    FileObject fo = _pyo.getPrimaryFile();

    if (fo != null && !fo.isValid()) {
      return null;
    }
    return fo;
  }

  @Override
  public void setDebugView(JpyDbgView view) {
    _view = view;
  }

  @Override
  public JpyDbgView getDebugView() {
    return _view;
  }

  @Override
  public void setSession(PythonSession pythonSession) {
    _pythonSession = pythonSession;
  }

  @Override
  public PythonSession getSession() {
    return _pythonSession;
  }

  public static Debuggee createDebuggee(FileObject fo) {
    Debuggee result = null;
    try {
      DataObject dataObject = DataObject.find(fo);
      if (dataObject != null) {
        result = new Debuggee(dataObject);
        synchronized (_debuggees) {
          _debuggees.put(fo, result);
        }
      }
    } catch (DataObjectNotFoundException e) {
      System.out.println("Cannot find DataObject for: " + fo + " :" + e.getMessage());
    }
    return result;
  }

  public static Debuggee getDebuggee(FileObject fo) {
    synchronized (_debuggees) {
      return _debuggees.get(fo);
    }

  }
}
