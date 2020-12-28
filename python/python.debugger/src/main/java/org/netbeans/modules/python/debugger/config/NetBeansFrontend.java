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
package org.netbeans.modules.python.debugger.config;

import org.openide.ErrorManager;

import org.openide.filesystems.*;
import org.openide.filesystems.FileObject;

import java.io.*;
import javax.swing.JOptionPane;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.python.debugger.PythonDebugException;
import org.netbeans.modules.python.debugger.PythonDebugParameters;
import org.netbeans.modules.python.debugger.Utils;

/**
 * IDEFrontEnd implementation for NetBeans
 *
 * @author jean-yves Mengant
 */
public class NetBeansFrontend {

  private final static String _JPYDBG_FATAL_MESSAGE =
          "nbPython debugger FATAL error";
  private final static String _NBPYTHON_DEBUG_HOME_ = "nbPython/debug";
  private final static String _WARNING_ = "nbPython debugger WARNING";
  private final static String _DEBUG_ = "nbPython debugger DEBUG";
  private final static String _INFO_ = "nbPython debugger INFO";
  private final static String _ERROR_ = "nbPython debugger ERROR";
  private final static String _SEPB_ = "[";
  private final static String _SEPE_ = "] ";
  private final static String _COLON_ = " : ";
  private final static boolean _DEBUG_FLAG_ = true;

  public static String getVersion() {
    return "nbPython debugger V0.0.12";
  }

  /**
   * Creates a new instance of NetBeansFrontend
   */
  public NetBeansFrontend() {
  }

  private static String format(Object source, String style, String message) {
    StringBuffer ret = new StringBuffer(_SEPB_);
    ret.append(style);
    ret.append(_SEPE_);
    ret.append(source.getClass().getName());
    ret.append(_COLON_);
    ret.append(message);

    return ret.toString();
  }

  /**
   * Error Logging facilities
   *
   * @param source  object error source
   * @param message message to be populated
   */
  public void logError(Object source, String message) {
    error(source, message);
  }

  /**
   * Error Logging allias
   *
   * @param source  object error source
   * @param message message to be populated
   */
  public final static void error(Object source, String message) {
    ErrorManager.getDefault().log(
            ErrorManager.ERROR,
            format(source, _ERROR_, message));
  }

  /**
   * populate Warnings
   *
   * @param source  object error source
   * @param message message to be populated
   */
  public void logWarning(Object source, String message) {
    warning(source, message);
  }

  /**
   * populate Warnings allias
   *
   * @param source  object error source
   * @param message message to be populated
   */
  public final static void warning(Object source, String message) {
    ErrorManager.getDefault().log(
            ErrorManager.WARNING,
            format(source, _WARNING_, message));
  }

  /**
   * populate debug infos
   *
   * @param source  object error source
   * @param message message to be populated
   */
  public void logDebug(Object source, String message) {
    debug(source, message);
  }

  /**
   * populate debug infos allias
   *
   * @param source  object error source
   * @param message message to be populated
   */
  public final static void debug(Object source, String message) {
    if (_DEBUG_FLAG_) {
      ErrorManager.getDefault().log(
              ErrorManager.INFORMATIONAL,
              format(source, _DEBUG_, message));
      System.out.println(format(source, _DEBUG_, message));
    }
  }

  /**
   * populate infos
   *
   * @param source  object error source
   * @param message message to be populated
   */
  public void logInfo(Object source, String message) {
    info(source, message);
  }

  /**
   * populate infos static alias
   *
   * @param source  object error source
   * @param message message to be populated
   */
  public final static void info(Object source, String message) {
    ErrorManager.getDefault().log(
            ErrorManager.INFORMATIONAL,
            format(source, _INFO_, message));
  }

  /**
   * file access facilities
   *
   * @param  fName source file Name
   *
   * @return corrspondind java Io File object
   *
   * @throws PythonDebugException DOCUMENT ME!
   */
  public File getFile(String fName) throws PythonDebugException {

    // enter home
    FileObject sfo = FileUtil.getConfigFile(_NBPYTHON_DEBUG_HOME_);
    try {
      if (sfo == null) {
        // first time inexisting create the directory
        sfo = FileUtil.createFolder(FileUtil.getConfigRoot(), _NBPYTHON_DEBUG_HOME_);
      }

      FileObject myFileObj = FileUtil.createData(sfo, fName);
      File f = FileUtil.toFile(myFileObj);

      return f;
    } catch (IOException e) {
      throw new PythonDebugException("getFile access failed on : " + fName);
    }
  }

  /**
   * Build file path from parent directory + file name
   *
   * @param  parent source path
   * @param  path   source file
   *
   * @return Absolute string file path
   */
  public String constructPath(String parent, String path) {
    File f = new File(parent, path);

    return f.getAbsolutePath();
  }

  /**
   * populate fatals back
   *
   * @param message message to be populated
   */
  public void populateFatalError(String message) {
    JOptionPane.showMessageDialog(
            null,
            message,
            _JPYDBG_FATAL_MESSAGE,
            JOptionPane.ERROR_MESSAGE);
  }

  /**
   * check for initializations
   */
  public static void initCheck() {
    if (!PythonDebugParameters.hasInited()) {
      // PythonDebugParameters.ideFront = new NetBeansFrontend();

      // populate global options to PythonDebugParameters statics
      // NbPythonDebuggerSettings.initGlobals() ;

      // Check for Python utilities initial loading
      PythonInstaller installer = new PythonInstaller();
      installer.putInPlace();
    }
  }

  /**
   * request Shortcut key to host IDE (To be implemented for netbeans)
   */
  public String getShortcutKeyInfo(String msginf, String shortCut) {
    return msginf; // no shortcut provided
  }

  /**
   * request given source to be populated inside Editor
   */
  public Object displaySource(String source) {
    return Utils.displaySource(source);
  }

  /**
   * Goto the File / line couple inside the editor
   *
   * @param  source candidate source
   * @param  line   candite line to show inside source
   *
   * @return the StyledDocument object
   */
  public Object displaySourceLine(String source, int line) {
    return Utils.gotoLine(source, line);
  }

  /**
   * send back the current selected focussed source full path info
   */
  public String getCurrentSource() {
    JTextComponent jtc = EditorRegistry.lastFocusedComponent();
    if (jtc != null) {
      Document doc = jtc.getDocument();
      System.out.println("NB currentSource =" + Utils.getDocumentSource(doc));
      return Utils.getDocumentSource(doc);
    } else {
      return null;
    }
  }

  /**
   * send back a neutral object buffer context (null if not jEdit)
   */
  public Object getBufferContext() {
    return null;
  }

  /**
   * get editor context back
   *
   * @param  fName candidate source file
   *
   * @return editor context
   */
  public Object getBufferContext(String fName) {
    return Utils.getDocumentFromFileName(fName);
  }

  public String getBufferPath(Object buffer) {
    return Utils.getDocumentSource((Document) buffer);
  }
}
