/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cnd.modelui.switcher;

import java.util.ArrayList;
import java.util.Collection;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.Presenter;

/**
 *
 */
public class CodeAssistanceSubmenuAction extends NodeAction {

    private LazyPopupMenu popupMenu;
    private final Collection<Action> items = new ArrayList<Action>(5);
    @Override
    public JMenuItem getPopupPresenter() {
        createSubMenu();
        return popupMenu;
    }

    @Override
    public JMenuItem getMenuPresenter() {
        createSubMenu();
        return popupMenu;
    }

    private void createSubMenu() {
        if (popupMenu == null) {
            popupMenu = new LazyPopupMenu(getName(), items); 
        }
        items.clear();
        items.addAll(Utilities.actionsForPath("NativeProjects/CodeAssistanceActions")); // NOI18N
        popupMenu.setEnabled(!items.isEmpty());
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return true;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(CodeAssistanceSubmenuAction.class, "LBL_CodeAssistanceAction_Name"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    private final static class LazyPopupMenu extends JMenu implements PopupMenuListener {
        private final Collection<Action> items;
        public LazyPopupMenu(String name, Collection<Action> items) {
            super(name);
            assert items != null : "array must be inited";
            this.items = items;
        }

        @Override
        public synchronized JPopupMenu getPopupMenu() {
            super.removeAll();
            // Some L&F call this method in constructor.
            // Work around bug #244444
            if (items != null) {
                for (Action action : items) {
                    if (action instanceof Presenter.Popup) {
                        JMenuItem item = ((Presenter.Popup)action).getPopupPresenter();
                        add(item);
                    } else if (action instanceof Presenter.Menu) {
                        JMenuItem item = ((Presenter.Menu)action).getMenuPresenter();
                        add(item);
                    } else {
                        add(action);
                    }
                }
            }
            JPopupMenu out = super.getPopupMenu();
            out.removePopupMenuListener(this);
            out.addPopupMenuListener(this);
            return out;
        }

        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            if (e.getSource() instanceof JPopupMenu) {
                ((JPopupMenu)e.getSource()).removePopupMenuListener(this);
            }
            super.removeAll();
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
        }
    }
}
