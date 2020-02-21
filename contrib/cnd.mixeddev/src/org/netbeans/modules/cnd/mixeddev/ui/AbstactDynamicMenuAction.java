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
