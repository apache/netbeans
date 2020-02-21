/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 */
package org.netbeans.modules.cnd.mixeddev.ui;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.modules.cnd.mixeddev.MixedDevUtils;
import org.netbeans.modules.cnd.mixeddev.wizard.GenerateProjectAction;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.Presenter;

/**
 *
 */
/*package*/ abstract class AbstactDynamicMenuAction extends AbstractAction implements ContextAwareAction {

    private final RequestProcessor requestProcessor;

    private final String menuName;

    public AbstactDynamicMenuAction(RequestProcessor requestProcessor, String menuName) {
        this.requestProcessor = requestProcessor;
        this.menuName = menuName;
    }

    @Override
    public boolean isEnabled() {
        // Do not show this item in Tools main menu
        // In other cases Context-aware instance will be created
        // TODO: how to make this correctly?
        return false;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        // Not used
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        final JMenu menu = new JMenu();
        menu.setText(menuName);
        int counter = 0;
        List<MenuItemWrapperAction> wrappers = new ArrayList();
        Action actions[] = createActions(actionContext);
        for (Action action : actions) {
            menu.add(collect(wrappers, new MenuItemWrapperAction(counter++, action)));
        }
        for (MenuItemWrapperAction action : wrappers) {
            action.init(menu, requestProcessor);
        }
        return new MenuWrapperAction(menu);
    }
    
    /**
     * 
     * @param actionContext
     * @return array of actions for menu
     */
    protected abstract Action[] createActions(Lookup actionContext);
    
    private MenuItemWrapperAction collect(List<MenuItemWrapperAction> wrappers, MenuItemWrapperAction action) {
        wrappers.add(action);
        return action;
    }

    private static class MenuWrapperAction extends AbstractAction implements Presenter.Popup {
        
        private final JMenu menu;

        public MenuWrapperAction(JMenu menu) {
            this.menu = menu;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Not used
        }

        @Override
        public JMenuItem getPopupPresenter() {
            return menu;
        }
    }
    
    private static class MenuItemWrapperAction extends AbstractAction {
        
        private final int position;
        
        private final Action delegate;

        public MenuItemWrapperAction(int position, Action delegate) {
            super(String.valueOf(delegate.getValue(Action.NAME)));
            this.position = position;
            this.delegate = delegate;
        }
        
        public void init(JMenu menu, RequestProcessor requestProcessor) {
            if (position < menu.getItemCount()) {
                final JMenuItem item = menu.getItem(position);
                item.setEnabled(false);
                requestProcessor.post(new Runnable() {
                    @Override
                    public void run() {
                        item.setEnabled(delegate.isEnabled());
                    }
                });
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            delegate.actionPerformed(e);
        }
    }
}
