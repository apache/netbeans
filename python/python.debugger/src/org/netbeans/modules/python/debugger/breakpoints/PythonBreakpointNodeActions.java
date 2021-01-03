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
