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
