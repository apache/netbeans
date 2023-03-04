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

package org.netbeans.modules.java.project.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import static org.netbeans.modules.java.project.ui.Bundle.*;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.Presenter;

/**
 * Popup menu in Projects tab permitting you to change the package view type.
 * @author Jesse Glick
 */
@ActionID(id="org.netbeans.modules.java.project.ChangePackageViewTypeAction", category="Project")
@ActionRegistration(lazy=false, displayName="#ChangePackageViewTypeAction_NAME")
@ActionReference(path=/* ProjectsRootNode.ACTIONS_FOLDER */"ProjectsTabActions", position=1600)
@Messages("ChangePackageViewTypeAction_NAME=Change Package View Type")
public final class ChangePackageViewTypeAction extends AbstractAction implements Presenter.Popup {
    
    public ChangePackageViewTypeAction() {
        putValue(Action.NAME, ChangePackageViewTypeAction_NAME()); // NOI18N
    }

    @Override public void actionPerformed(ActionEvent e) {
        assert false : e;
    }

    @Messages({
        "LBL_change_package_type=&View Java Packages as",
        "ChangePackageViewTypeAction_list=&List",
        "ChangePackageViewTypeAction_tree=&Tree",
        "ChangePackageViewTypeAction_reduced_tree=&Reduced Tree"
    })
    @Override public JMenuItem getPopupPresenter() {
        JMenu menu = new JMenu();
        Mnemonics.setLocalizedText(menu, LBL_change_package_type());
        menu.add(createChoice(JavaProjectSettings.PackageViewType.PACKAGES, ChangePackageViewTypeAction_list()));
        menu.add(createChoice(JavaProjectSettings.PackageViewType.TREE, ChangePackageViewTypeAction_tree()));
        menu.add(createChoice(JavaProjectSettings.PackageViewType.REDUCED_TREE, ChangePackageViewTypeAction_reduced_tree()));
        return menu;
    }
    
    private JMenuItem createChoice(final JavaProjectSettings.PackageViewType type, String label) {
        JRadioButtonMenuItem item = new JRadioButtonMenuItem();
        Mnemonics.setLocalizedText(item, label);
        item.setSelected(JavaProjectSettings.getPackageViewType() == type);
        item.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                JavaProjectSettings.setPackageViewType(type);
            }
        });
        return item;
    }
    
}
