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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
