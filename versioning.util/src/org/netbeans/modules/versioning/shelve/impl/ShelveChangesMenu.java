/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.versioning.shelve.impl;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.modules.versioning.shelve.ShelveChangesActionsRegistry;
import org.netbeans.modules.versioning.shelve.ShelveChangesActionsRegistry.ShelveChangesActionProvider;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Actions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;
import org.openide.windows.TopComponent;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.versioning.shelve.impl.ShelveChangesMenu", category = "Versioning/Additional")
@ActionRegistration(displayName = "#CTL_Menu_ShelveChanges")
@NbBundle.Messages("CTL_Menu_ShelveChanges=&Shelve Changes")
public class ShelveChangesMenu extends AbstractAction implements Presenter.Menu {
    public static final String PREF_KEY_SHELVED_PATCHES = "shelvedPatches"; //NOI18N
    
    public ShelveChangesMenu () {
        super();
    }

    @Override
    public JMenuItem getMenuPresenter () {
        JMenu menu = createMenu();
        return menu;
    }
    
    public static List<JComponent> getMenuActions (VCSContext context, Lookup lkp) {
        List<JComponent> menuItems = new ArrayList<>();
        VersioningSystem [] vs = Utils.getOwners(context);
        // shelve changes
        List<JComponent> shelveActions = getShelveActions(vs, lkp);
        menuItems.addAll(shelveActions);
        
        // unshelve changes
        List<JComponent> unshelveActions = getUnshelveActions(vs, context, lkp);
        if (!shelveActions.isEmpty() && !unshelveActions.isEmpty()) {
            menuItems.add(null);
        }
        menuItems.addAll(unshelveActions);
        return menuItems;
    }

    private JMenu createMenu () {
        JMenu menu = new JMenu(this);
        org.openide.awt.Mnemonics.setLocalizedText(menu, Bundle.CTL_Menu_ShelveChanges());
        VCSContext ctx = VCSContext.forNodes(TopComponent.getRegistry().getActivatedNodes());
        
        for (JComponent item : getMenuActions(ctx, null)) {
            if (item == null) {
                menu.addSeparator();
            } else {
                menu.add(item);
            }
        }
        enableMenu(menu);
        return menu;
    }

    private static List<JComponent> getShelveActions (VersioningSystem[] vs, Lookup lkp) {
        List<JComponent> items = new ArrayList<>();
        

        if (vs.length == 1) {
            // actions depending on the central patch storage
            ShelveChangesActionProvider actionProvider = ShelveChangesActionsRegistry.getInstance().getActionProvider(vs[0]);
            Action action;
            if (actionProvider != null && (action = actionProvider.getAction()) != null && action.isEnabled()) {
                if (lkp != null) {
                    action = SystemActionBridge.createAction(action, Actions.cutAmpersand((String) action.getValue(Action.NAME)), lkp);
                }
                JMenuItem item = new JMenuItem();
                Actions.connect(item, action, false);
                items.add(item);
            }
            // actions using their own logic
            for (Action a : Utilities.actionsForPath("Actions/Versioning/ShelveChanges")) {
                if (lkp != null) {
                    a = SystemActionBridge.createAction(a, Actions.cutAmpersand((String) a.getValue(Action.NAME)), lkp);
                }
                JMenuItem item = new JMenuItem();
                Actions.connect(item, a, false);
                items.add(item);
            }
        }
        return items;
    }

    private static List<JComponent> getUnshelveActions (VersioningSystem[] vs, VCSContext ctx, Lookup lkp) {
        List<JComponent> items = new ArrayList<>();
        List<String> list = Utils.getStringList(NbPreferences.forModule(ShelveChangesMenu.class), PREF_KEY_SHELVED_PATCHES);
        // XXX use Actions.forID
        Action a = Utils.getAcceleratedAction("Actions/Versioning/UnshelveChanges/org-netbeans-modules-versioning-shelve-impl-UnshelveChangesAction.instance");
        if (a != null && !list.isEmpty()) {
            JMenuItem mItem = new JMenuItem();
            if (lkp != null) {
                a = SystemActionBridge.createAction(a, Actions.cutAmpersand((String) a.getValue(Action.NAME)), lkp);
            }
            Actions.connect(mItem, a, false);
            items.add(mItem);
        }
        if (!list.isEmpty()) {
            List<PatchStorage.Patch> patches = PatchStorage.getInstance().getPatches();
            int i = 0;
            for (ListIterator<PatchStorage.Patch> it = patches.listIterator(); it.hasNext() && ++i < 6; ) {
                PatchStorage.Patch patch = it.next();
                JMenuItem mItem = new JMenuItem();
                Actions.connect(mItem, new UnshelveChangesAction(patch.getPatchName()), false);
                items.add(mItem);
            }
        }
        if (vs.length == 1) {
            // actions depending on the central patch storage
            ShelveChangesActionProvider actionProvider = ShelveChangesActionsRegistry.getInstance().getActionProvider(vs[0]);
            JComponent[] components;
            if (actionProvider != null && (components = actionProvider.getUnshelveActions(ctx, lkp != null)).length > 0) {
                items.add(null);
                items.addAll(Arrays.asList(components));
            }
        }
        return items;
    }

    @Override
    public void actionPerformed (ActionEvent e) {
        //
    }

    private void enableMenu (JMenu menu) {
        boolean enabled = false;
        for (int i = 0; i < menu.getItemCount(); ++i) {
            JMenuItem item = menu.getItem(i);
            if (item != null && item.isEnabled()) {
                enabled = true;
                break;
            }
        }
        menu.setEnabled(enabled);
    }

}
