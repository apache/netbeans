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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import org.netbeans.modules.form.*;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

public class BindAction extends CookieAction {

    @Override
    protected int mode() {
        return MODE_EXACTLY_ONE; // can be invoked on just one node
    }

    @Override
    protected Class[] cookieClasses() {
        return new Class[] { RADComponentCookie.class };
    }

    @Override
    public String getName() {
        return "Bind"; // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
    }

    @Override
    public JMenuItem getMenuPresenter() {
        return getPopupPresenter();
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu popupMenu = new JMenu(NbBundle.getMessage(BindAction.class, "ACT_Bind")); // NOI18N
        
        popupMenu.setEnabled(isEnabled());
        
        popupMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                JMenu menu = (JMenu) e.getSource();
                createBindingsSubmenu(menu);
            }
            
            @Override
            public void menuDeselected(MenuEvent e) {}
            
            @Override
            public void menuCanceled(MenuEvent e) {}
        });
        return popupMenu;
    }

    private void createBindingsSubmenu(JMenu menu) {
        if (menu.getMenuComponentCount() > 0)
            menu.removeAll();

        Node[] nodes = getActivatedNodes();
        if (nodes.length != 1)
            return;

        RADComponentCookie radCookie = nodes[0].getCookie(RADComponentCookie.class);
        if (radCookie == null)
            return;

        BindingProperty[][] bindingProps = radCookie.getRADComponent().getBindingProperties();
        BindingProperty[] props = bindingProps[(bindingProps[0].length==0) ? 1 : 0];
        if (props.length > 0) {
            for (BindingProperty prop : props) {
                BindingMenuItem mi = new BindingMenuItem(prop);
                mi.addActionListener(mi);
                menu.add(mi);
            }
        } else {
            JMenuItem item = new JMenuItem(NbBundle.getMessage(BindAction.class, "MSG_NoBinding")); // NOI18N
            item.setEnabled(false);
            menu.add(item);
        }
    }

    private static class BindingMenuItem extends JMenuItem implements ActionListener {
        private BindingProperty bindingProperty;

        private BindingMenuItem(BindingProperty prop) {
            bindingProperty = prop;
            setText(prop.getDisplayName());
            updateFont();
        }

        private void updateFont() {
            java.awt.Font font = getFont();
            if (bindingProperty.getValue() != null) {
                setFont(font.deriveFont(font.getStyle() | java.awt.Font.BOLD));
            }
            else {
                setFont(font.deriveFont(font.getStyle() & ~java.awt.Font.BOLD));
            }
        }

        @Override
        public void actionPerformed(ActionEvent ev) {
            MetaBinding binding = bindingProperty.getValue();
            final BindingCustomizer customizer = new BindingCustomizer(bindingProperty);
            customizer.setBinding(binding);
            customizer.getDialog(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ev) {
                    bindingProperty.setValue(customizer.getBinding());
                    updateFont();
                }
            }).setVisible(true);
        }
    }
}
