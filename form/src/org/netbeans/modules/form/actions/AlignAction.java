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
