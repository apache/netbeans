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
package org.netbeans.modules.dlight.sendto.action;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.netbeans.modules.dlight.sendto.api.Configuration;
import org.netbeans.modules.dlight.sendto.api.ConfigurationsRegistry;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.DataEditorSupport;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;

/**
 * Meta action to be registered in Tools menu....
 */
@ActionID(id = "org.netbeans.modules.dlight.sendto.action.MenuMetaAction", category = "Tools/SendTo")
@ActionRegistration(displayName = "#SendToMenuName", lazy = false)
@ActionReferences(value = {
    @ActionReference(path = "Editors/Popup", name = "sendToAction", position = 1917),
    @ActionReference(path = "Editors/TabActions", name = "sendToAction", position = 1917),
    @ActionReference(path = "UI/ToolActions", name = "sendToAction"/*, position = 1917*/)
})
public class MenuMetaAction extends SystemAction implements ContextAwareAction {

    @Override
    public boolean isEnabled() {
        // Do not show this item in Tools main menu
        // In other cases Context-aware instance will be created
        // TODO: how to make this correctly?
        return false;
    }

    @Override
    public String getName() {
        return "unused-name"; // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("sendto-execute"); // NOI18N
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new MenuWrapperAction(actionContext);
    }

    private static class MenuWrapperAction extends AbstractAction implements Presenter.Popup {

        private final LazyMenu menu;
        private final Lookup actionContext;

        public MenuWrapperAction(Lookup actionContext) {
            this.actionContext = actionContext;
            this.menu = new LazyMenu();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
        }

        @Override
        public JMenuItem getPopupPresenter() {
            return menu;
        }

        @Override
        public boolean isEnabled() {
            // fast check
            return isFileObject() || isEditorSelection();
        }

        private boolean isFileObject() {
            final Collection<? extends FileObject> fos = actionContext.lookupAll(FileObject.class);

            if (fos == null || fos.isEmpty()) {
                return false;
            }
            return true;
        }

        private boolean isEditorSelection() {
            DataEditorSupport des = actionContext.lookup(DataEditorSupport.class);
            if (des == null) {
                return false;
            }
            DataObject dao = des.getDataObject();
            if (dao == null) {
                return false;
            }
            final Collection<? extends FileObject> fos = actionContext.lookupAll(FileObject.class);

            if (fos == null || fos.isEmpty()) {
                return false;
            }
            return fos.contains(dao.getPrimaryFile());
        }

        private class LazyMenu extends DynamicMenu {

            private boolean initialized;

            public LazyMenu() {
                super(NbBundle.getMessage(MenuConstructor.class, "SendToMenuName"));
            }

            @Override
            public JPopupMenu getPopupMenu() {
                if (!initialized) {
                    final List<Configuration> configs = ConfigurationsRegistry.getConfigurations();

                    if (configs == null) {
                        menu.setEmpty();
                    } else {
                        initialized = true;
                        MenuUpdator.start(menu, actionContext, configs);
                    }
                }
                return super.getPopupMenu();
            }
        }
    }
}
