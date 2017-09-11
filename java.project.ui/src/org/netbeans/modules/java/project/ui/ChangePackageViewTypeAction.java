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
