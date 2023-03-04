/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.refactoring.spi.impl;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JMenuItem;
import org.netbeans.modules.refactoring.api.impl.ActionsImplementationFactory;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.openide.actions.RenameAction;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

import org.openide.util.actions.SystemAction;
import org.openide.util.actions.Presenter.Menu;
import org.openide.util.actions.Presenter.Popup;

/** Action that displays refactoring submenu action on JavaDataObject
 * and delegates to it.
 *
 * @author Martin Matula
 * @author Jan Becicka
 */
public class RSMDataObjectAction extends SystemAction implements Menu, Popup, ContextAwareAction {
    // create delegate (RefactoringSubMenuAction)
    private final RefactoringSubMenuAction action = new RefactoringSubMenuAction(false);
    
    private static final RenameAction renameAction = SystemAction.get(RenameAction.class);
    
    @Override
    public void actionPerformed(ActionEvent ev) {
        // do nothing -- should never be called
    }
    
    @Override
    public String getName() {
        return (String) action.getValue(Action.NAME);
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new HelpCtx(RSMEditorActionAction.class);
    }
    
    @Override
    public JMenuItem getMenuPresenter() {
        return action.getMenuPresenter();
    }
    
    @Override
    public JMenuItem getPopupPresenter() {
        return action.getPopupPresenter();
    }
    
    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        if (ActionsImplementationFactory.canRename(actionContext)) {
            return this;
        } else {
            Action rename = renameAction.createContextAwareInstance(actionContext);
            if (!rename.isEnabled() && ActionsImplementationFactory.canDelete(actionContext)) {
                return this;
            } else {
                return rename;
            }
        }
    }
}
    
