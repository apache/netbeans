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

package org.netbeans.modules.form.actions;

import javax.swing.*;

import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;

import org.netbeans.modules.form.palette.PaletteItem;
import org.netbeans.modules.form.palette.PaletteMenuView;
import org.netbeans.modules.form.*;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * Action allowing to choose a component from palette content and add it to
 * the selected containers in current form. Presented only in contextual menus
 * within the Form Editor.
 *
 * @author Tomas Pavek
 */

public class AddAction extends CallableSystemAction {

    private static String name;

    @Override
    public String getName() {
        if (name == null)
            name = org.openide.util.NbBundle.getBundle(AddAction.class)
                     .getString("ACT_Add"); // NOI18N
        return name;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(AddAction.class);
    }

    @Override
    public boolean isEnabled() {
        Node[] nodes = getNodes();
        for (int i=0; i < nodes.length; i++) {
            FormCookie formCookie = nodes[i].getCookie(FormCookie.class);
            if (formCookie == null)
                return false;

            RADComponentCookie radCookie = nodes[i].getCookie(RADComponentCookie.class);
            if (radCookie != null
                  && !(radCookie.getRADComponent() instanceof ComponentContainer))
                return false;
        }
        return true;
    }

    @Override
    public JMenuItem getMenuPresenter() {
        return getPopupPresenter();
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenuItem menu = new PaletteMenuView(
            new NodeAcceptor() {
            @Override
                public boolean acceptNodes(Node[] nodes) {
                    if (nodes.length != 1)
                        return false;

                    PaletteItem paletteItem = nodes[0].getCookie(PaletteItem.class);
                    if (paletteItem == null)
                        return false;

                    nodes = getNodes();
                    if (nodes.length == 0)
                        return false;

                    boolean added = false;

                    for (int i=0; i < nodes.length; i++) {
                        FormCookie formCookie = nodes[i].getCookie(FormCookie.class);
                        if (formCookie == null)
                            continue;

                        RADComponentCookie radCookie = nodes[i].getCookie(RADComponentCookie.class);
                        RADComponent targetComponent;
                        if (radCookie != null) {
                            targetComponent = radCookie.getRADComponent();
                            if (!(targetComponent instanceof ComponentContainer))
                                continue;
                        }
                        else targetComponent = null;

                        FormModel formModel = formCookie.getFormModel();
                        if (formModel.getComponentCreator().createComponent(
                                paletteItem, targetComponent, null)
                                != null) {
                            added = true;
                        }
                    }

                    return added;
                }
            }
        );

        menu.setText(getName());
        menu.setEnabled(isEnabled());
        HelpCtx.setHelpIDString(menu, AddAction.class.getName());
        return menu;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public void performAction() {
    }

    // -------

    private static Node[] getNodes() {
        // using NodeAction and global activated nodes is not reliable
        // (activated nodes are set with a delay after selection in
        // ComponentInspector)
        return ComponentInspector.getInstance().getExplorerManager().getSelectedNodes();
    }
}
