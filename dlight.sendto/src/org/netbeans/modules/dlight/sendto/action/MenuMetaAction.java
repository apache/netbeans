/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
