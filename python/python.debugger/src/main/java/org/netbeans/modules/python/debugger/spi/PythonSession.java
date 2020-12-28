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

import java.io.File;
import org.netbeans.modules.python.debugger.Debuggee;
import org.netbeans.modules.python.debugger.actions.JpyDbgView;

/**
 * A Python debugging session instance
 * @author jean-yves Mengant
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
