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

package org.netbeans.modules.git.ui.menu;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.modules.git.Annotator;
import org.netbeans.modules.git.ui.commit.ExcludeFromCommitAction;
import org.netbeans.modules.git.ui.commit.IncludeInCommitAction;
import org.netbeans.modules.git.ui.ignore.IgnoreAction;
import org.netbeans.modules.git.ui.ignore.UnignoreAction;
import org.openide.util.NbBundle;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.Actions;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;

/**
 * Container menu for ignore/exclude actions.
 *
 * @author Ondra Vrabec
 */
@NbBundle.Messages({
    "CTL_MenuItem_IgnoreMenu=&Ignore",
    "CTL_MenuItem_IgnoreMenu.popupName=Ignore"
})
public final class IgnoreMenu extends DynamicMenu {

    private final Lookup lkp;

    public IgnoreMenu (Lookup lkp) {
        super(lkp == null ? Bundle.CTL_MenuItem_IgnoreMenu() : Bundle.CTL_MenuItem_IgnoreMenu_popupName());
        this.lkp = lkp;
    }
    
    @Override
    protected JMenu createMenu() {
        JMenu menu = new JMenu(this);
        JMenuItem item;
        if (lkp == null) {
            Action ia = SystemAction.get(IgnoreAction.class);
            Action uia = SystemAction.get(UnignoreAction.class);
            Action efca = SystemAction.get(ExcludeFromCommitAction.class);
            Action iica = SystemAction.get(IncludeInCommitAction.class);
            
            if (ia.isEnabled()) {
                item = new JMenuItem();
                Utils.setAcceleratorBindings(Annotator.ACTIONS_PATH_PREFIX, ia);
                Actions.connect(item, ia, false);
                menu.add(item);
            }
            if (uia.isEnabled()) {
                item = new JMenuItem();
                Utils.setAcceleratorBindings(Annotator.ACTIONS_PATH_PREFIX, uia);
                Actions.connect(item, uia, false);
                menu.add(item);
            }
            if (efca.isEnabled()) {
                item = new JMenuItem();
                Utils.setAcceleratorBindings(Annotator.ACTIONS_PATH_PREFIX, efca);
                Actions.connect(item, efca, false);
                menu.add(item);
            } else if (iica.isEnabled()) {
                item = new JMenuItem();
                Utils.setAcceleratorBindings(Annotator.ACTIONS_PATH_PREFIX, iica);
                Actions.connect(item, iica, false);
                menu.add(item);
            }
        } else {
            SystemActionBridge ia = SystemActionBridge.createAction(SystemAction.get(IgnoreAction.class),
                    NbBundle.getMessage(IgnoreAction.class, "LBL_IgnoreAction_PopupName"), lkp);
            SystemActionBridge uia = SystemActionBridge.createAction(SystemAction.get(UnignoreAction.class),
                    NbBundle.getMessage(UnignoreAction.class, "LBL_UnignoreAction_PopupName"), lkp);
            SystemActionBridge efca = SystemActionBridge.createAction(SystemAction.get(ExcludeFromCommitAction.class),
                    NbBundle.getMessage(ExcludeFromCommitAction.class, "LBL_ExcludeFromCommitAction_PopupName"), lkp);
            SystemActionBridge iica = SystemActionBridge.createAction(SystemAction.get(IncludeInCommitAction.class),
                    NbBundle.getMessage(IncludeInCommitAction.class, "LBL_IncludeInCommitAction_PopupName"), lkp);
            if (ia.isEnabled() || uia.isEnabled() || efca.isEnabled() || iica.isEnabled()) {
                if (ia.isEnabled()) {
                    item = menu.add(ia);
                    org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
                }
                if (uia.isEnabled()) {
                    item = menu.add(uia);
                    org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
                }
                if (efca.isEnabled()) {
                    item = menu.add(efca);
                    org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
                } else if (iica.isEnabled()) {
                    item = menu.add(iica);
                    org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
                }
            }
        }        
        return menu;
    }
}
