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
package org.netbeans.modules.python.debugger.breakpoints;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import java.util.Set;
import java.util.Collections;
import javax.swing.JComponent;
import org.netbeans.modules.python.debugger.Utils;
import org.netbeans.modules.python.debugger.actions.JpyDbgView;
import org.netbeans.api.debugger.ActionsManager;
import org.openide.text.Line;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.spi.debugger.ui.Controller;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.util.WeakListeners;

public class PythonBreakpointActionProvider
        extends ActionsProviderSupport
        implements PropertyChangeListener {

  private final static Set _ACTIONS_ = Collections.singleton(
          ActionsManager.ACTION_TOGGLE_BREAKPOINT);

  /**
   * Creates a new instance of PythonBreakpointActionProvider
   */
  public PythonBreakpointActionProvider() {
    setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT, true);
    TopComponent.getRegistry().addPropertyChangeListener(
            WeakListeners.propertyChange(this, TopComponent.getRegistry()));
  }

  private void removeFromJpyDbg(PythonBreakpoint bp) {
    if (JpyDbgView.get_debuggerView() != null) // if JpyDbgView is active populate info to debugger immediatly
    {
      JpyDbgView.get_debuggerView().clearBreakPoint(bp);
    }
  }

  private void addToJpyDbg(PythonBreakpoint bp) {
    if (JpyDbgView.get_debuggerView() != null) // if JpyDbgView is active populate info to debugger immediatly
    {
      JpyDbgView.get_debuggerView().setBreakPoint(bp);
    }

  }

  /**
   * Called when the action is called (action button is pressed).
   *
   * @param action an action which has been called
   */
  @Override
  public void doAction(Object action) {
    Line line = Utils.getCurrentLine();

    Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
    int i, k = breakpoints.length;

    for (i = 0; i < k; i++) {
      if (breakpoints[i] instanceof PythonBreakpoint &&
              (((PythonBreakpoint) breakpoints[i]).getLine() != null) &&
              ((PythonBreakpoint) breakpoints[i]).getLine().equals(line)) {
        // assume breakpoint removal
        removeFromJpyDbg((PythonBreakpoint) breakpoints[i]);
        DebuggerManager.getDebuggerManager().removeBreakpoint(breakpoints[i]);
        break;
      }
    }
    if (i == k) {
      // not there => assume breakpoint add
      PythonBreakpoint bp = new PythonBreakpoint(line);
      addToJpyDbg(bp);
      DebuggerManager.getDebuggerManager().addBreakpoint(bp);
    }
  }

  /**
   * Returns set of actions supported by this ActionsProvider.
   *
   * @return set of actions supported by this ActionsProvider
   */
  @Override
  public Set getActions() {
    return _ACTIONS_;
  }

  public static JComponent getCustomizerComponent(Breakpoint b) {
    return new PythonBreakpointPanel((PythonBreakpoint) b);

  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    boolean enabled = Utils.getCurrentLine() != null;
    setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT, enabled);
  }

  public static void customize(Breakpoint b) {
    JComponent c = getCustomizerComponent(b);
    HelpCtx helpCtx = HelpCtx.findHelp(c);
    if (helpCtx == null) {
      helpCtx = new HelpCtx("debug.add.breakpoint");  // NOI18N
    }
    final Controller[] cPtr = new Controller[]{(Controller) c};
    final DialogDescriptor[] descriptorPtr = new DialogDescriptor[1];
    final Dialog[] dialogPtr = new Dialog[1];
    ActionListener buttonsActionListener = new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent ev) {
        if (descriptorPtr[0].getValue() == DialogDescriptor.OK_OPTION) {
          boolean ok = cPtr[0].ok();
          if (ok) {
            dialogPtr[0].setVisible(false);
          }
        } else {
          dialogPtr[0].setVisible(false);
        }
      }
    };
    DialogDescriptor descriptor = new DialogDescriptor(
            c,
            NbBundle.getMessage(
            PythonBreakpointActionProvider.class,
            "CTL_Breakpoint_Customizer_Title" // NOI18N
            ),
            true,
            DialogDescriptor.OK_CANCEL_OPTION,
            DialogDescriptor.OK_OPTION,
            DialogDescriptor.DEFAULT_ALIGN,
            helpCtx,
            buttonsActionListener);
    descriptor.setClosingOptions(new Object[]{});
    Dialog d = DialogDisplayer.getDefault().createDialog(descriptor);
    d.pack();
    descriptorPtr[0] = descriptor;
    dialogPtr[0] = d;
    d.setVisible(true);
  }
}
