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

package org.netbeans.modules.refactoring.spi.impl;

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.openide.awt.Actions;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 * It is UI placeholder for language specific refactoring actions
 *
 * @author Jan Pokorsky
 * @see RefactoringContextActionsProvider
 */
public final class RefactoringContextAction extends AbstractAction implements ContextAwareAction, Presenter.Menu, Presenter.Popup {

    private final Lookup context;

    public RefactoringContextAction() {
        this(null);
    }

    public RefactoringContextAction(Lookup context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported."); // NOI18N
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new RefactoringContextAction(actionContext);
    }

    @Override
    public JMenuItem getMenuPresenter() {
        return new InlineMenu(context, false);
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return new InlineMenu(context, true);
    }

    private static final class InlineMenu extends JMenuItem implements DynamicMenuContent {

        private static final JComponent[] EMPTY_CONTENT = new JComponent[0];
        private Lookup context;
        private final boolean popup;

        public InlineMenu(Lookup context, boolean popup) {
            this.context = context;
            this.popup = popup;
        }

        @Override
        public JComponent[] getMenuPresenters() {
            return createMenuItems();
        }

        @Override
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            JComponent[] comps = new JComponent[1];
            for (JComponent item : items) {
                if (item instanceof Actions.MenuItem) {
                    comps[0] = item;
                    // update menu items to reflect Action.isEnabled
                    ((Actions.MenuItem) item).synchMenuPresenters(comps);
                } else if(item instanceof JMenu) {
                    JMenu jMenu = (JMenu) item;
                    for (Component subItem : jMenu.getMenuComponents()) {
                        if (subItem instanceof Actions.MenuItem) {
                            comps[0] = (JComponent) subItem;
                            // update menu items to reflect Action.isEnabled
                            ((Actions.MenuItem) subItem).synchMenuPresenters(comps);
                        }
                    }
                }
            }
            // returns most up-to date items
            return createMenuItems();
        }

        /** Creates items when actually needed. */
        private JComponent[] createMenuItems() {
            resolveContext();
            MimePath mpath = resolveMIMEType();

            RefactoringContextActionsProvider actionProvider = mpath != null
                    ? MimeLookup.getLookup(mpath).lookup(RefactoringContextActionsProvider.class)
                    : null;

            if (actionProvider != null) {
                return actionProvider.getMenuItems(popup, context);
            } else {
                return EMPTY_CONTENT;
            }

        }

        private void resolveContext() {
            if (context == null) {
                context = Utilities.actionsGlobalContext();
            }
        }

        private MimePath resolveMIMEType() {
            MimePath mpath = context.lookup(MimePath.class);
            if (mpath != null) {
                return mpath;
            }

            FileObject fobj = context.lookup(FileObject.class);
            if (fobj != null) {
                return MimePath.parse(fobj.getMIMEType());
            }
            
            DataObject dobj = context.lookup(DataObject.class);
            return dobj == null ? null : MimePath.parse(dobj.getPrimaryFile().getMIMEType());
        }

    }

}
