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

package org.netbeans.modules.python.project2.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import static org.netbeans.modules.python.project2.ui.Bundle.*;

/**
 * Popup menu in Projects tab permitting you to change the package view type.
 *
 * <p>
 * <b>This is copied from the corresponding Java action in java.projects</b>
 * </p>
 *
 */
@NbBundle.Messages({"LBL_change_package_type=&View Python Packages as",
    "ChangePackageViewTypeAction_list=&List",
    "ChangePackageViewTypeAction_tree=&Tree"})
public final class ChangePackageViewTypeAction extends AbstractAction implements Presenter.Popup {
    
    public ChangePackageViewTypeAction() {}

    @Override
    public void actionPerformed(ActionEvent e) {
        assert false : e;
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu menu = new JMenu();
        Mnemonics.setLocalizedText(menu, LBL_change_package_type());
        menu.add(createChoice(PythonProjectSettings.TYPE_PACKAGE_VIEW, ChangePackageViewTypeAction_list()));
        menu.add(createChoice(PythonProjectSettings.TYPE_TREE, ChangePackageViewTypeAction_tree()));
        return menu;
    }
    
    private JMenuItem createChoice(final int type, String label) {
        JRadioButtonMenuItem item = new JRadioButtonMenuItem();
        Mnemonics.setLocalizedText(item, label);
        item.setSelected(PythonProjectSettings.getPackageViewType() == type);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PythonProjectSettings.setPackageViewType(type);
            }
        });
        return item;
    }
    
}
