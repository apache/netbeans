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

package org.netbeans.modules.project.ui.groups;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.JButton;
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
import org.netbeans.modules.project.uiapi.Utilities;
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
        "GroupsMenu.new_cancel=Cancel"
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
        panel.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (NewGroupPanel.PROP_READY.equals(evt.getPropertyName())) {
                    create.setEnabled(panel.isReady());
                }
            }
        });
        JButton cancel = new JButton(GroupsMenu_new_cancel());
        dd.setOptions(new Object[] {create, cancel});
        Object result = DialogDisplayer.getDefault().notify(dd);
        if (result.equals(create)) {
            assert panel.isReady();
            final NewGroupPanel.Type type = panel.getSelectedType();
            final boolean autoSync = panel.isAutoSyncField();
            final boolean useOpen = panel.isUseOpenedField();
            final String name = panel.getNameField();
            final String masterProject = panel.getMasterProjectField();
            final String directory = panel.getDirectoryField();
            RP.post(new Runnable() {
                @Override
                public void run() {
                    Group g = NewGroupPanel.create(type, name, autoSync, useOpen, masterProject, directory);
                    Group.setActiveGroup(g, true);
                }
            });
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
        "GroupsMenu.manage_cancel=&Cancel",
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
        panel.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("selection")) {
                    select.setEnabled(panel.isExactlyOneGroupSelected());
                }
            }
        });
        select.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        Group.setActiveGroup(panel.getSelectedGroups()[0], false);
                    }
                });
            }
        });
        final JButton newGroup = new JButton();
        newGroup.setDefaultCapable(false);
        Mnemonics.setLocalizedText(newGroup, GroupsMenu_manage_new_group());
        newGroup.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                newGroup();
            }
        });
        JButton cancel = new JButton(GroupsMenu_new_cancel());
        cancel.setDefaultCapable(false);
        dd.setOptions(new Object[] {select, newGroup, cancel});
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
                                             new ActionListener() {
                                                @Override
                                                public void actionPerformed(ActionEvent ae) {
                                                    //noop
                                                }
                                            }, 
                                             new ActionListener() {
                                                @Override
                                                public void actionPerformed(ActionEvent ae) {
                                                    //noop
                                                }
                                             }, new HelpCtx(HELPCTX));
            dialog.setTitle( GroupsMenu_properties_title() );
            dialog.setModal(true);
            dialog.setVisible(true);
    }

}
