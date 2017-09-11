/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
    
