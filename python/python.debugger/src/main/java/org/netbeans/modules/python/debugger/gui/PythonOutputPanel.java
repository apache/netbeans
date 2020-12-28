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
package org.netbeans.modules.python.debugger.gui;

import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.netbeans.modules.python.api.PythonOptions;
import org.netbeans.modules.python.debugger.utils.CommandLineListener;
import org.netbeans.modules.python.debugger.utils.SwingMessageArea;

/**
 * A basic StdOut panel for Debugging or running instances
 * @author jean-yves Mengant
 */
public class PythonOutputPanel
        extends JPanel {

  /** not null => tabbed pane container */
  private JTabbedPane _hostPane = null;
  /** if not -1 => tabbedPane index */
  private int _pos = -1;
  private SwingMessageArea _setOutTrace;

  private void init() {
    PythonOptions pyOptions = PythonOptions.getInstance();

    _setOutTrace = new SwingMessageArea(pyOptions.getDbgShellFont(),
            pyOptions.getDbgShellBackground(),
            pyOptions.getDbgShellHeaderColor(),
            pyOptions.getDbgShellErrorColor(),
            pyOptions.getDbgShellWarningColor(),
            pyOptions.getDbgShellInfoColor());
    _setOutTrace.set_refresh(true);

    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, _setOutTrace);

  }

  /** Creates a new instance of PythonOutputPanel */
  public PythonOutputPanel(JTabbedPane hostPane,
          ImageIcon icon,
          CommandLineListener cmdListener) {
    init();
    _setOutTrace.addCommandLineListener(cmdListener);
    _hostPane = hostPane;
    _hostPane.addTab("stdout content", icon, this);
    _pos = _hostPane.indexOfComponent(this);
  }

  /** Creates a new instance of PythonOutputPanel */
  public PythonOutputPanel() {
    init();
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (_hostPane != null) {
      _hostPane.setEnabledAt(_pos, enabled);
    }
  }

  public void pauseShell() {
    if (_setOutTrace != null) {
      _setOutTrace.hasEnabled(false);
    }
  }

  public void writeMessage(String msg) {
    _setOutTrace.message(msg);
  }

  public void messageAppend(String msg) {
    _setOutTrace.messageAppend(msg);
  }

  public void headerAppend(String msg) {
    _setOutTrace.headerAppend(msg);
  }

  public void writeError(String msg) {
    _setOutTrace.error(msg);
  }

  public void writeHeader(String msg) {
    _setOutTrace.headerFooter(msg);
  }

  public void writeLog(String msg) {
    _setOutTrace.headerFooter("[LOG]" + msg);
  }

  public void writeWarning(String msg) {
    _setOutTrace.warning(msg);
  }

  public void checkColoringChanges(PythonOptions pyOptions) {
    _setOutTrace.populateGUIInfos(pyOptions.getDbgShellFont(),
            pyOptions.getDbgShellBackground(),
            pyOptions.getDbgShellHeaderColor(),
            pyOptions.getDbgShellErrorColor(),
            pyOptions.getDbgShellWarningColor(),
            pyOptions.getDbgShellInfoColor());
  }

  public void debuggingShellColorChanged(PythonOptions options) {
    checkColoringChanges(options);
  }
}

