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
package org.netbeans.modules.php.codeception.ui.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.testing.PhpTesting;
import org.netbeans.modules.php.codeception.CodeceptionTestingProvider;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Actions;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

/**
 * Factory for Codeception actions.
 */
@ActionID(id = "org.netbeans.modules.php.codeception.ui.actions.CodeceptionActionsFactory", category = "Project")
@ActionRegistration(displayName = "#CodeceptionActionsFactory.name", lazy = false)
@ActionReference(path = "Projects/org-netbeans-modules-php-project/Actions", position = 950)
@NbBundle.Messages("CodeceptionActionsFactory.name=Codeception")
public final class CodeceptionActionsFactory extends AbstractAction implements ContextAwareAction, Presenter.Popup {

    @NullAllowed
    private final PhpModule phpModule;


    public CodeceptionActionsFactory() {
        this(null);
    }

    public CodeceptionActionsFactory(PhpModule phpModule) {
        this.phpModule = phpModule;
        setEnabled(phpModule != null);
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        // hide this action from Tools > Keymap
        putValue(Action.NAME, ""); // NOI18N
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        assert false;
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        PhpModule module = PhpModule.Factory.lookupPhpModule(actionContext);
        if (module == null) {
            return this;
        }
        if (!PhpTesting.isTestingProviderEnabled(CodeceptionTestingProvider.IDENTIFIER, module)) {
            return this;
        }
        return new CodeceptionActionsFactory(module);
    }

    @Override
    public JMenuItem getPopupPresenter() {
        if (phpModule == null) {
            return new Actions.MenuItem(this, false);
        }
        JMenu menu = new JMenu(Bundle.CodeceptionActionsFactory_name());
        menu.add(new BootstrapAction(phpModule));
        menu.add(new BuildAction(phpModule));
        menu.add(new CleanAction(phpModule));
        return menu;
    }

}
