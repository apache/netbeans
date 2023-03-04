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
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import javax.swing.*;

import org.openide.util.HelpCtx;
import org.openide.util.actions.*;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import org.netbeans.modules.form.*;

/**
 * Class used to represent the parent hierarchy of a single selected component in
 * the UI editor. This allows the user to quickly jump to editing a parent without
 * having to move their mouse to the inspector window and perform an action.
 * 
 * @author Wade Chandler
 */

public class DesignParentAction extends NodeAction {
    
    @Override
    protected boolean enable(Node[] nodes) {
        boolean ret = false;
        if (nodes != null && nodes.length == 1){
            RADComponentCookie radCookie = nodes[0].getCookie(RADComponentCookie.class);
            RADComponent comp = (radCookie != null) ? radCookie.getRADComponent() : null;
            if (comp != null && isParentEditableComponent(comp)) {
                FormDesigner designer = getDesigner(comp);
                if (designer != null) {
                    RADVisualComponent designed = designer.getTopDesignComponent();
                    if (comp == designed
                        || (designed != null && designed.isParentComponent(comp)
                            && isParentEditableComponent(comp.getParentComponent()))) {
                        ret = true;
                        // the component must be in the designed tree and have
                        // some designable parent that is not designed at this moment
                    }
                }
            }
        }
        return ret;
    }

    public static boolean isParentEditableComponent(RADComponent comp) {
        RADComponent parent = comp.getParentComponent();
        if (EditContainerAction.isEditableComponent(parent)) {
            return true;
        }
        // if not having parent, consider the top component
        if (parent == null) {
            RADComponent topComp = comp.getFormModel().getTopRADComponent();
            return comp != topComp && EditContainerAction.isEditableComponent(topComp);
        }
        return false;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
    }

    static void reenable(Node[] nodes) {
        SystemAction.get(DesignParentAction.class).reenable0(nodes);
    }

    private void reenable0(Node[] nodes) {
        setEnabled(enable(nodes));
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(DesignParentAction.class, "ACT_DesignParentAction"); // NOI18N
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
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
                NbBundle.getMessage(DesignParentAction.class, "ACT_DesignParentAction")); // NOI18N
        
        popupMenu.setEnabled(isEnabled());
        HelpCtx.setHelpIDString(popupMenu, DesignParentAction.class.getName());
        createSubmenu(popupMenu);
        return popupMenu;
    }
    
    private void createSubmenu(JMenu menu) {
        Node[] nodes = getActivatedNodes();
        if (nodes != null && nodes.length == 1) {
            RADComponentCookie radCookie = nodes[0].getCookie(RADComponentCookie.class);
            RADComponent comp = (radCookie != null) ? radCookie.getRADComponent() : null;
            if (comp != null) {
                List<JMenuItem> list = new ArrayList<JMenuItem>();
                RADComponent topComp = comp.getFormModel().getTopRADComponent();
                boolean topCompIncluded = false;
                RADComponent parent = comp.getParentComponent();
                while (parent != null) {
                    list.add(new DesignParentMenuItem(parent, parent == topComp, getMenuItemListener()));
                    if (parent == topComp) {
                        topCompIncluded = true;
                    }
                    parent = parent.getParentComponent();
                }
                if (!topCompIncluded && topComp != null) {
                    list.add(new DesignParentMenuItem(topComp, true, getMenuItemListener()));
                }
                for (ListIterator<JMenuItem> it=list.listIterator(list.size()); it.hasPrevious(); ) {
                    menu.add(it.previous());
                }
            }
        }
    }
    
    private ActionListener getMenuItemListener() {
        if (menuItemListener == null)
            menuItemListener = new DesignParentMenuItemListener();
        return menuItemListener;
    }

    private static FormDesigner getDesigner(RADComponent comp) {
        return FormEditor.getFormDesigner(comp.getFormModel());
    }

    private static class DesignParentMenuItem extends JMenuItem {
        private RADComponent radc = null;
        public DesignParentMenuItem(RADComponent c, boolean top, ActionListener l){
            radc = c;
            setText(top ? NbBundle.getMessage(DesignParentAction.class, "ACT_DesignParentTopMenuItemName") // NOI18N
                          : c.getName());
            addActionListener(l);
            FormDesigner designer = getDesigner(c);
            setEnabled(designer != null && c != designer.getTopDesignComponent());
        }
        public RADComponent getRADComponent(){
            return this.radc;
        }
    }

    private static class DesignParentMenuItemListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt) {
            
            Object source = evt.getSource();
            if( source == null || !(source instanceof DesignParentMenuItem) ){
                return;
            }
            
            DesignParentMenuItem mi = (DesignParentMenuItem)source;
            
            RADComponent lc = mi.getRADComponent();
            FormDesigner designer = getDesigner(lc);
            if (designer != null) {
                designer.setTopDesignComponent((RADVisualComponent)lc, true);
                designer.requestActive();

                // NodeAction is quite unreliable in enabling, do it ourselves for sure
                Node[] n = new Node[] { lc.getNodeReference() };
                if (n[0] != null) {
                    EditContainerAction.reenable(n);
                    DesignParentAction.reenable(n);
                    EditFormAction.reenable(n);
                }
            }
        }
    }

    private ActionListener menuItemListener;
}
