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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.form.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import org.netbeans.modules.form.ComponentInspector;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.RADComponentNode;
import org.netbeans.modules.form.actions.AlignAction;
import org.netbeans.modules.form.palette.PaletteItem;
import org.netbeans.modules.form.palette.PaletteUtils;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Action class providing popup menu presenter for add submenu for JMenu components.
 *
 * @author Joshua Marinacci, Jan Stola
 */
public class AddSubItemAction extends NodeAction {
    
    //fix this
    @Override
    protected boolean enable(Node[] nodes) {
        return true; 
    }
    
    @Override
    public String getName() {
        return NbBundle.getMessage(AddSubItemAction.class, "ACT_AddFromPalette"); // NOI18N
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
     * 
     * @return the JMenuItem representation for the action
     */
    @Override
    public JMenuItem getPopupPresenter() {
        JMenu popupMenu = new JMenu(NbBundle.getMessage(AddSubItemAction.class, "ACT_AddFromPalette")); //NOI18N
        
        popupMenu.setEnabled(isEnabled());
        HelpCtx.setHelpIDString(popupMenu, AlignAction.class.getName());
        
        popupMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                JMenu menu = (JMenu) e.getSource();
                createInsertSubmenu(menu);
            }
            
            @Override
            public void menuDeselected(MenuEvent e) {}
            
            @Override
            public void menuCanceled(MenuEvent e) {}
        });
        return popupMenu;
    }

    
    private class AddListener implements ActionListener {
        private PaletteItem pItem;
        
        public AddListener(PaletteItem pItem) {
            this.pItem = pItem;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Node[] nds = getNodes();
            for(Node nd : nds) {
                if(nd instanceof RADComponentNode) {
                    RADComponentNode rnode = (RADComponentNode) nd;
                    RADComponent comp = rnode.getRADComponent();
                    MenuEditLayer.addComponentToEndOfMenu(comp, pItem);
                }
            }
        }
    };
    
    
    private void createInsertSubmenu(JMenu menu) {
        //only create this menu the first time it is called
        if (!(menu.getMenuComponentCount() > 0)) {
            Set<Class> classes = new HashSet<Class>();
            SortedSet<PaletteItem> items = new TreeSet<PaletteItem>(new Comparator<PaletteItem>() {
                @Override
                public int compare(PaletteItem item1, PaletteItem item2) {
                    String name1 = item1.getNode().getDisplayName();
                    String name2 = item2.getNode().getDisplayName();
                    return name1.compareTo(name2);
                }
            });
            for (PaletteItem item : PaletteUtils.getAllItems()) {
                Class clazz = item.getComponentClass();
                if ((clazz != null) && !classes.contains(clazz) &&
                        (JMenuItem.class.isAssignableFrom(clazz) || JSeparator.class.isAssignableFrom(clazz))) {
                    classes.add(clazz);
                    items.add(item);
                }
            }
            for (PaletteItem item : items) {
                JMenuItem menuitem = new JMenuItem(item.getNode().getDisplayName());
                menuitem.addActionListener(new AddListener(item));
                menu.add(menuitem);
            }
        }
    }

    private static Node[] getNodes() {
        // using NodeAction and global activated nodes is not reliable
        // (activated nodes are set with a delay after selection in
        // ComponentInspector)
        return ComponentInspector.getInstance().getExplorerManager().getSelectedNodes();
    }
}
