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
package org.netbeans.modules.python.debugger.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.netbeans.modules.python.debugger.config.NetBeansFrontend;
import org.openide.util.NbBundle;
import org.openide.util.ImageUtilities;

/**
 * Action which shows PythonDebugging Console component.
 */
public class PythonDebugConsoleAction extends AbstractAction {

  public static String ICON_PATH = "org/netbeans/modules/python/debugger/actions/bugicon.gif";

  public PythonDebugConsoleAction() {
    super(NbBundle.getMessage(PythonDebugConsoleAction.class, "CTL_PythonDebugConsoleAction"));
    putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_PATH, true));
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
//        PythonConsoleTopComponent win = PythonConsoleTopComponent.findInstance();
//        if (win.nTerm() > 1)
//            win.newTab();
//        else{
//            win.open();
//        }
//        win.requestActive();
    // first check for correct initializations
    // of IDE frontend module
    NetBeansFrontend.initCheck();
    openPythonDebuggingWindow();
  }

  private void openPythonDebuggingWindow() {
    JpyDbgView jv = JpyDbgView.getCurrentView();
    jv.openPythonDebuggingWindow(null, true);
  }
}
