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

package org.netbeans.modules.mercurial.ui.menu;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.modules.mercurial.MercurialAnnotator;
import org.netbeans.modules.mercurial.ui.queues.QCreatePatchAction;
import org.netbeans.modules.mercurial.ui.queues.QDiffAction;
import org.netbeans.modules.mercurial.ui.queues.QFinishPatchesAction;
import org.netbeans.modules.mercurial.ui.queues.QGoToPatchAction;
import org.netbeans.modules.mercurial.ui.queues.QPopAllAction;
import org.netbeans.modules.mercurial.ui.queues.QPushAllPatchesAction;
import org.netbeans.modules.mercurial.ui.queues.QRefreshPatchAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.Actions;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter;

/**
 * Container menu for mqueues actions.
 *
 * @author Ondra Vrabec
 */
@NbBundle.Messages({
    "CTL_MenuItem_QueuesMenu=&Queues",
    "CTL_MenuItem_QueuesMenu.popupName=Queues"
})
public class QueuesMenu extends DynamicMenu implements Presenter.Popup {
    private final Lookup lkp;

    public QueuesMenu (Lookup lkp) {
        super(Bundle.CTL_MenuItem_QueuesMenu());
        this.lkp = lkp;
    }

    @Override
    protected JMenu createMenu() {
        JMenu menu = new JMenu(this);
        JMenuItem item;
        if (lkp == null) {
            org.openide.awt.Mnemonics.setLocalizedText(menu, NbBundle.getMessage(QueuesMenu.class, "CTL_MenuItem_QueuesMenu")); // NOI18N
            item = new JMenuItem();
            Action action = SystemAction.get(QDiffAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            item = new JMenuItem();
            action = SystemAction.get(QGoToPatchAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            item = new JMenuItem();
            action = SystemAction.get(QPopAllAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            item = new JMenuItem();
            action = SystemAction.get(QPushAllPatchesAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            menu.addSeparator();
            
            item = new JMenuItem();
            action = SystemAction.get(QCreatePatchAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            item = new JMenuItem();
            action = SystemAction.get(QRefreshPatchAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            item = new JMenuItem();
            action = SystemAction.get(QFinishPatchesAction.class);
            Utils.setAcceleratorBindings(MercurialAnnotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
        } else {
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(QDiffAction.class), NbBundle.getMessage(QDiffAction.class, "CTL_PopupMenuItem_QDiff"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(QGoToPatchAction.class), NbBundle.getMessage(QGoToPatchAction.class, "CTL_PopupMenuItem_QGoToPatch"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(QPopAllAction.class), NbBundle.getMessage(QPopAllAction.class, "CTL_PopupMenuItem_QPopAllPatches"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(QPushAllPatchesAction.class), NbBundle.getMessage(QPushAllPatchesAction.class, "CTL_PopupMenuItem_QPushAllPatches"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            menu.addSeparator();
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(QCreatePatchAction.class), NbBundle.getMessage(QCreatePatchAction.class, "CTL_PopupMenuItem_QCreatePatch"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(QRefreshPatchAction.class), NbBundle.getMessage(QRefreshPatchAction.class, "CTL_PopupMenuItem_QRefreshPatch"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(QFinishPatchesAction.class), NbBundle.getMessage(QFinishPatchesAction.class, "CTL_PopupMenuItem_QFinishPatches"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
        }        
        return menu;
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu menu = createMenu();
        menu.setText(Bundle.CTL_MenuItem_QueuesMenu_popupName());
        enableMenu(menu);
        return menu;
    }
}
