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
 * Action class providing popup menu presenter for setresizability submenu.
 *
 * @author Martin Grebac
 */

public class SetResizabilityAction extends NodeAction {

    private JCheckBoxMenuItem[] items;
    
    @Override
    protected boolean enable(Node[] nodes) {
        List comps = FormUtils.getSelectedLayoutComponents(nodes);
        return ((comps != null) && (comps.size() > 0));
    }
    
    @Override
    public String getName() {
        return NbBundle.getMessage(SetResizabilityAction.class, "ACT_SetResizability"); // NOI18N
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
            NbBundle.getMessage(SetResizabilityAction.class, "ACT_SetResizability")); // NOI18N
        
        popupMenu.setEnabled(isEnabled());
        HelpCtx.setHelpIDString(popupMenu, SetResizabilityAction.class.getName());
        
        popupMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                JMenu menu = (JMenu) e.getSource();
                createResizabilitySubmenu(menu);
            }
            
            @Override
            public void menuDeselected(MenuEvent e) {}
            
            @Override
            public void menuCanceled(MenuEvent e) {}
        });
        return popupMenu;
    }

    private void createResizabilitySubmenu(JMenu menu) {
        Node[] nodes = getActivatedNodes();
        List components = FormUtils.getSelectedLayoutComponents(nodes);
        if ((components == null) || (components.size() < 1)) {
            return;
        }
        if (!(menu.getMenuComponentCount() > 0)) {
            ResourceBundle bundle = NbBundle.getBundle(SetResizabilityAction.class);

            JCheckBoxMenuItem hItem = new ResizabilityMenuItem(
                    bundle.getString("CTL_ResizabilityH"), // NOI18N
                    components,
                    0);
            JCheckBoxMenuItem vItem = new ResizabilityMenuItem(
                    bundle.getString("CTL_ResizabilityV"), // NOI18N
                    components,
                    1);
            items = new JCheckBoxMenuItem[] {hItem, vItem};
            
            for (int i=0; i<2; i++) {
                items[i].addActionListener(getMenuItemListener());
                HelpCtx.setHelpIDString(items[i], SetResizabilityAction.class.getName());
                menu.add(items[i]);
            }
        }
        updateState(components);
    }

    private void updateState(List components) {
        if ((components == null) || (components.size()<1)) {
            return;
        }
        RADComponent rc = (RADComponent)components.get(0);
        FormDesigner formDesigner = FormEditor.getFormDesigner(rc.getFormModel());
        formDesigner.updateResizabilityActions();
        for (int i=0; i<2; i++) {
            Action a = formDesigner.getResizabilityActions()[i];
            items[i].setEnabled(a.isEnabled());
            items[i].setSelected(Boolean.TRUE.equals(a.getValue(Action.SELECTED_KEY)));
        }
    }
    
    private ActionListener getMenuItemListener() {
        if (menuItemListener == null)
            menuItemListener = new ResizabilityMenuItemListener();
        return menuItemListener;
    }

    // --------

    private static class ResizabilityMenuItem extends JCheckBoxMenuItem {
        private int direction;
        private List components;

        ResizabilityMenuItem(String text, List components, int direction) {
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

    private static class ResizabilityMenuItemListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt) {
            Object source = evt.getSource();
            if (!(source instanceof ResizabilityMenuItem)) {
                return;
            }
            ResizabilityMenuItem mi = (ResizabilityMenuItem) source;
            if (!mi.isEnabled()) {
                return;
            }
            int index = mi.getDirection();
            RADComponent radC = (RADComponent)mi.getRADComponents().get(0);
            FormModel fm = radC.getFormModel();
            FormDesigner fd = FormEditor.getFormDesigner(fm);
            Action a = fd.getResizabilityActions()[index];
            a.putValue(Action.SELECTED_KEY, !Boolean.TRUE.equals(a.getValue(Action.SELECTED_KEY)));
            a.actionPerformed(evt);
        }
    }
        
    private ActionListener menuItemListener;
}
