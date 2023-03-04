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

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.event.MenuListener;
import javax.swing.event.MenuEvent;

import org.openide.util.HelpCtx;
import org.openide.util.actions.*;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import org.netbeans.modules.form.*;

/**
 * Action class providing popup menu presenter for align submenu.
 *
 * @author Martin Grebac
 */

public class AlignAction extends NodeAction {

    private JMenuItem[] items;
    
    @Override
    protected boolean enable(Node[] nodes) {
        List comps = FormUtils.getSelectedLayoutComponents(nodes);
        return ((comps != null) && (comps.size() > 1));
    }
    
    @Override
    public String getName() {
        return NbBundle.getMessage(AlignAction.class, "ACT_Align"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected void performAction(Node[] activatedNodes) { }

    @Override
    public JMenuItem getMenuPresenter() {
        return getPopupPresenter();
    }

    /**
     * Returns a JMenuItem that presents this action in a Popup Menu.
     * @return the JMenuItem representation for the action
     */
    @Override
    public JMenuItem getPopupPresenter() {
        JMenu popupMenu = new JMenu(
            NbBundle.getMessage(AlignAction.class, "ACT_Align")); // NOI18N
        
        popupMenu.setEnabled(isEnabled());
        HelpCtx.setHelpIDString(popupMenu, AlignAction.class.getName());
        
        popupMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                JMenu menu = (JMenu) e.getSource();
                createAlignSubmenu(menu);
            }
            
            @Override
            public void menuDeselected(MenuEvent e) {}
            
            @Override
            public void menuCanceled(MenuEvent e) {}
        });
        return popupMenu;
    }


    private void createAlignSubmenu(JMenu menu) {
        Node[] nodes = getActivatedNodes();
        List components = FormUtils.getSelectedLayoutComponents(nodes);
        if (!(menu.getMenuComponentCount() > 0)) {
            ResourceBundle bundle = NbBundle.getBundle(AlignAction.class);

            JMenuItem leftGroupItem = new AlignMenuItem(
                    bundle.getString("CTL_GroupLeft"), // NOI18N
                    components,
                    0);
            JMenuItem rightGroupItem = new AlignMenuItem(
                    bundle.getString("CTL_GroupRight"), // NOI18N
                    components,
                    1);
            JMenuItem centerHGroupItem = new AlignMenuItem(
                    bundle.getString("CTL_GroupHCenter"), // NOI18N
                    components,
                    2);
            JMenuItem upGroupItem = new AlignMenuItem(
                    bundle.getString("CTL_GroupUp"), // NOI18N
                    components,
                    3);
            JMenuItem downGroupItem = new AlignMenuItem(
                    bundle.getString("CTL_GroupDown"), // NOI18N
                    components,
                    4);
            JMenuItem centerVGroupItem = new AlignMenuItem(
                    bundle.getString("CTL_GroupVCenter"), // NOI18N
                    components,
                    5);
            JMenuItem leftItem = new AlignMenuItem(
                    bundle.getString("CTL_AlignLeft"), // NOI18N
                    components,
                    6);
            JMenuItem rightItem = new AlignMenuItem(
                    bundle.getString("CTL_AlignRight"), // NOI18N
                    components,
                    7);
            JMenuItem upItem = new AlignMenuItem(
                    bundle.getString("CTL_AlignUp"), // NOI18N
                    components,
                    8);
            JMenuItem downItem = new AlignMenuItem(
                    bundle.getString("CTL_AlignDown"), // NOI18N
                    components,
                    9);
            items = new JMenuItem[] {leftGroupItem, rightGroupItem, centerHGroupItem,
                upGroupItem, downGroupItem, centerVGroupItem, leftItem, rightItem,
                upItem, downItem};
            for (int i=0; i < items.length; i++) {
                items[i].addActionListener(getMenuItemListener());
                items[i].setEnabled(false);
                HelpCtx.setHelpIDString(items[i], AlignAction.class.getName());
                menu.add(items[i]);
                if (i == 5) {
                    menu.addSeparator();
                }
            }
        }
        updateState(components);
    }

    private void updateState(List components) {
        if ((components == null) || (components.size()<2)) {
            return;
        }
        RADComponent rc = (RADComponent)components.get(0);
        FormDesigner formDesigner = FormEditor.getFormDesigner(rc.getFormModel());
        java.util.Collection<Action> col = formDesigner.getDesignerActions(true);
        assert (items.length == 10);
        int n = col.size();
        Action[] actions = col.toArray(new Action[n]);
        for (int i=0; i < n; i++) {
            items[i].setEnabled(actions[i].isEnabled());
        }
        items[n].setEnabled(actions[0].isEnabled());
        items[n+1].setEnabled(actions[1].isEnabled());
        items[n+2].setEnabled(actions[3].isEnabled());
        items[n+3].setEnabled(actions[4].isEnabled());
    }
    
    private ActionListener getMenuItemListener() {
        if (menuItemListener == null)
            menuItemListener = new AlignMenuItemListener();
        return menuItemListener;
    }

    // --------

    private static class AlignMenuItem extends JMenuItem {
        private int direction;
        private List components;

        AlignMenuItem(String text, List components, int direction) {
            super(text);
            this.components = components;
            this.direction = direction;
        }
        
        int getDirection() {
            return direction;
        }

        List getRADComponents() {
            return components;
        }
    }

    private static class AlignMenuItemListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt) {
            Object source = evt.getSource();
            if (!(source instanceof AlignMenuItem)) {
                return;
            }
            AlignMenuItem mi = (AlignMenuItem) source;
            if (!mi.isEnabled()) {
                return;
            }
            int index = mi.getDirection();
            RADComponent radC = (RADComponent)mi.getRADComponents().get(0);
            FormModel fm = radC.getFormModel();
            FormDesigner fd = FormEditor.getFormDesigner(fm);
            ((Action)fd.getDesignerActions(false).toArray()[index]).actionPerformed(evt);            
        }
    }
        
    private ActionListener menuItemListener;
}
