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
