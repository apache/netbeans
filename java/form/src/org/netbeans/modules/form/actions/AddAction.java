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
