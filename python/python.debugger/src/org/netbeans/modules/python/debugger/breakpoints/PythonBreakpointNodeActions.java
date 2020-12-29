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

import javax.swing.Action;
import org.netbeans.modules.python.debugger.Utils;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.Models.ActionPerformer;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
 * Provides actions for nodes representing {@link PythonBreakpoint} in the
 * Breapoint view.
 *
 */
public final class PythonBreakpointNodeActions implements NodeActionsProviderFilter {

  private static final Action GO_TO_SOURCE_ACTION;
  private static final Action PROPERTIES_ACTION;

  static {
    String name = "GoTo Source";
    ActionPerformer ap = new ActionPerformer() {

      @Override
      public boolean isEnabled(Object node) {
        return true;
      }

      @Override
      public void perform(Object[] nodes) {
        PythonBreakpoint bp = (PythonBreakpoint) nodes[0];
        Utils.showLine(Utils.getLineAnnotatable(bp.getFilePath(), bp.getLineNumber() - 1));
      }
    };
    GO_TO_SOURCE_ACTION = Models.createAction(name, ap, Models.MULTISELECTION_TYPE_EXACTLY_ONE);
  }

  static {
    String name = "Properties ";
    ActionPerformer ap = new ActionPerformer() {

      @Override
      public boolean isEnabled(Object node) {
        return true;
      }

      @Override
      public void perform(Object[] nodes) {
        PythonBreakpoint bp = (PythonBreakpoint) nodes[0];
        PythonBreakpointActionProvider.customize(bp);
      }
    };
    PROPERTIES_ACTION = Models.createAction(name, ap, Models.MULTISELECTION_TYPE_EXACTLY_ONE);
  }

  @Override
  public void performDefaultAction(NodeActionsProvider original, Object node) throws UnknownTypeException {
    if (node instanceof PythonBreakpoint) {
      PythonBreakpoint bp = (PythonBreakpoint) node;
      Utils.showLine(Utils.getLineAnnotatable(bp.getFilePath(), bp.getLineNumber() - 1));
    } else {
      original.performDefaultAction(node);
    }
  }

  @Override
  public Action[] getActions(NodeActionsProvider original, Object node) throws UnknownTypeException {
    Action[] origActions = original.getActions(node);
    if (node instanceof PythonBreakpoint) {
      Action[] actions = new Action[origActions.length + 4];
      actions[0] = GO_TO_SOURCE_ACTION;
      actions[1] = null;
      System.arraycopy(origActions, 0, actions, 2, origActions.length);
      actions[origActions.length] = null;
      actions[origActions.length + 1] = PROPERTIES_ACTION;
      return actions;
    } else {
      return origActions;
    }
  }
}
