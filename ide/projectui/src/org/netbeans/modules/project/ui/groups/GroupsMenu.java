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

package org.netbeans.modules.project.ui.groups;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.netbeans.modules.project.ui.ProjectsRootNode;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import static org.netbeans.modules.project.ui.groups.Bundle.*;
import org.netbeans.modules.project.uiapi.BaseUtilities;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Menu listing available groups and offering some operations on them.
 * @author Jesse Glick
 */
@ActionID(id = "org.netbeans.modules.project.ui.groups.GroupsMenu", category = "Project")
@ActionRegistration(displayName = "#GroupsMenu.label", lazy=false)
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 1100),
    @ActionReference(path = ProjectsRootNode.ACTIONS_FOLDER, position = 600, separatorAfter = 700)
})
@Messages("GroupsMenu.label=Project Gro&ups...")
public class GroupsMenu extends AbstractAction {
    
    private static final RequestProcessor RP = new RequestProcessor(GroupsMenu.class.getName());
    private static final String HELPCTX = "org.netbeans.modules.project.ui.groups.GroupsMenu";

    public GroupsMenu() {
        super(GroupsMenu_label());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        manageGroups();
    }

    /**
     * Create (and open) a new group.
     */
    @Messages({
        "GroupsMenu.new_title=Create New Group",
        "GroupsMenu.new_create=Create Group",
        "GroupsMenu.new_close=Close"
    })
    private static void newGroup() {
        final NewGroupPanel panel = new NewGroupPanel();
        DialogDescriptor dd = new DialogDescriptor(panel, GroupsMenu_new_title());
        panel.setNotificationLineSupport(dd.createNotificationLineSupport());
        dd.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);
        dd.setModal(true);
        dd.setHelpCtx(new HelpCtx(HELPCTX));
        final JButton create = new JButton(GroupsMenu_new_create());
        create.setDefaultCapable(true);
        create.setEnabled(panel.isReady());
        panel.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            if (NewGroupPanel.PROP_READY.equals(evt.getPropertyName())) {
                create.setEnabled(panel.isReady());
            }
        });
        JButton close = new JButton(GroupsMenu_new_close());
        dd.setOptions(new Object[] {create, close});
        Object result = DialogDisplayer.getDefault().notify(dd);
        if (result.equals(create)) {
            assert panel.isReady();
            final NewGroupPanel.Type type = panel.getSelectedType();
            final boolean autoSync = panel.isAutoSyncField();
            final boolean useOpen = panel.isUseOpenedField();
            final String name = panel.getNameField();
            final String masterProject = panel.getMasterProjectField();
            final String directory = panel.getDirectoryField();
            RP.post(() -> {
                Group g = NewGroupPanel.create(type, name, autoSync, useOpen, masterProject, directory);
                Group.setActiveGroup(g, true);
                SwingUtilities.invokeLater(GroupsMenu::manageGroups);
            });
        } else {
            SwingUtilities.invokeLater(GroupsMenu::manageGroups);
        }
    }
    
    /**
     * Manage groups.
     */
    @Messages({
        "GroupsMenu.manage_title=Manage Groups",
        "GroupsMenu.manage_select_group=&Select Group",
        "GroupsMenu.manage_new_group=&New Group...",
        "GroupsMenu.manage_remove=&Remove",
        "GroupsMenu.manage_close=Close",
        "GroupsMenu.manage_properties=&Properties",
    })
    private static void manageGroups() {
        final ManageGroupsPanel panel = new ManageGroupsPanel();
        DialogDescriptor dd = new DialogDescriptor(panel, GroupsMenu_manage_title());
        dd.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);
        dd.setModal(true);
        dd.setHelpCtx(new HelpCtx(HELPCTX));
        final JButton select = new JButton();
        Mnemonics.setLocalizedText(select, GroupsMenu_manage_select_group());
        select.setDefaultCapable(true);
        panel.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            if (evt.getPropertyName().equals("selection")) {
                select.setEnabled(panel.isExactlyOneGroupSelected());
            }
        });
        select.addActionListener((ActionEvent e) -> {
            RP.post(() -> Group.setActiveGroup(panel.getSelectedGroups()[0], false));
        });
        final JButton newGroup = new JButton();
        newGroup.setDefaultCapable(false);
        Mnemonics.setLocalizedText(newGroup, GroupsMenu_manage_new_group());
        // invokeLater ensures that the parent is disposed before the new dialog opens
        // so that it can set a parent which doesn't disappear - fixes race condition
        newGroup.addActionListener(e -> SwingUtilities.invokeLater(GroupsMenu::newGroup));
        JButton close = new JButton(GroupsMenu_manage_close());
        close.setDefaultCapable(false);
        dd.setOptions(new Object[] {select, newGroup, close});
        DialogDisplayer.getDefault().notify(dd);
    }

    /**
     * Open a properties dialog for the group, according to its type.
     */
    @Messages("GroupsMenu.properties_title=Project Group Properties")
    static void openProperties(Group g) {
        Lookup context = Lookups.fixed(new Object[] { g, BaseUtilities.ACCESSOR.createGroup(g.getName(), g.prefs()) });
        Dialog dialog = ProjectCustomizer.createCustomizerDialog("Projects/Groups/Customizer", //NOI18N
                                         context, 
                                         (String)null,
                                         (ActionEvent ae) -> {},
                                         (ActionEvent ae) -> {},
                                         new HelpCtx(HELPCTX));
        dialog.setTitle( GroupsMenu_properties_title() );
        dialog.setModal(true);
        dialog.setVisible(true);
    }

}
