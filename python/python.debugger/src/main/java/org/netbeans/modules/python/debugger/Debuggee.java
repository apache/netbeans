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
 * @author Jean-Yves Mengant
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
