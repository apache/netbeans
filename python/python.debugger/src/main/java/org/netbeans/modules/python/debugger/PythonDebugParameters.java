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

import java.awt.*;
import java.io.File;
import org.netbeans.modules.python.api.PythonOptions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/** 
 * @author jean-yves Mengant
 *
 * this class is used to store python debuggers parameters
 * ( refactored to get everything from PythonOptions Core )
 */
public class PythonDebugParameters {

  private final static String _NBPYTHON_DEBUG_HOME_ = "nbPython/debug";
  /** Front End Multi IDE interface (MUST BE SET AT IDE PLUGIN INIT TIME)  by Ide Pluggin*/
  // public static NetBeansFrontend ideFront = null ;
  private final static String _EMPTY_ = "";
  private final static String _DEFAULT_HOST_ = "localhost";
  private final static String _JPYDBG_ = "jpydbg.py";


  /* PyLint Static Stuff ends here */
  private static boolean _hasInited = false;
  private static String _jpydbgScript = _JPYDBG_;
  private static String _tempDir = null;

  public static boolean hasInited() {
    if (_hasInited) {
      return true;
    }
    _hasInited = true;
    return false;
  }

  public static String get_codePage() {
    return _EMPTY_;
  }

  public static int get_listeningPort() {
    return PythonOptions.getInstance().getPythonDebuggingPort();
  }

  public static String get_dbgHost() {
    return _DEFAULT_HOST_;
  }

  public static boolean get_debugTrace() {
    return true;
  }

  public static String get_jpydbgScript() {
    return _jpydbgScript;
  }

  public static void set_jpydbgScript(String script) {
    _jpydbgScript = script;
  }

  public static void set_tempDir(String tempDir) {
    _tempDir = tempDir;
  }

  public static String get_tempDir() {
    return _tempDir;
  }

  public static String get_jpydbgScriptArgs() {
    return _EMPTY_;
  }

  public static int get_connectingPort() {
    return -1;
  }

  public static Color get_shellBackground() {
    return PythonOptions.getInstance().getDbgShellBackground();
  }

  public static Color get_shellError() {
    return PythonOptions.getInstance().getDbgShellErrorColor();
  }

  public static Font get_shellFont() {
    return PythonOptions.getInstance().getDbgShellFont();
  }

  public static Color get_shellHeader() {
    return PythonOptions.getInstance().getDbgShellHeaderColor();
  }

  public static Color get_shellMessage() {
    return PythonOptions.getInstance().getDbgShellInfoColor();
  }

  public static Color get_shellWarning() {
    return PythonOptions.getInstance().getDbgShellWarningColor();
  }

  public static String get_workDir() {
    return null;
  }

  public static File checkSettingsDirectory() {
    FileObject netbeansRoot = FileUtil.getConfigRoot();
    File root = FileUtil.toFile(netbeansRoot);
    File nbPythonDebugHome = new File(root, _NBPYTHON_DEBUG_HOME_);
    if (nbPythonDebugHome.isDirectory()) {
      return nbPythonDebugHome;
    }
    nbPythonDebugHome.mkdirs();
    return nbPythonDebugHome;
  }

  /**
   * where is the setting directory
   *
   * @return the path location
   */
  public static String getSettingsDirectory() {
    File f = checkSettingsDirectory();

    return f.getAbsolutePath();
  }
}
